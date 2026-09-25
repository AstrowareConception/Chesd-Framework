package fr.astroware.chess.bot.situation;

import fr.astroware.chess.bot.analysis.Analysis;
import fr.astroware.chess.bot.rule.PresenceDetection;
import fr.astroware.chess.bot.rule.Situation;
import fr.astroware.chess.bot.situation.detection.CaptureDetection;
import fr.astroware.chess.core.model.Color;
import fr.astroware.chess.core.model.Move;
import fr.astroware.chess.core.model.Piece;
import fr.astroware.chess.core.model.PlacedPiece;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * Point d'entrée vers les situations fournies par le framework.
 *
 * <p>Le catalogue est volontairement progressif : chaque nouvelle situation
 * doit être réutilisable par plusieurs bots et s'appuyer sur les services
 * communs d'analyse plutôt que recalculer sa propre vision de l'échiquier.</p>
 */
public final class Situations {

    private Situations() {
    }

    /**
     * Situation toujours reconnue.
     */
    public static Situation<PresenceDetection> always() {
        return context -> List.of(PresenceDetection.INSTANCE);
    }

    /**
     * Détecte toutes les captures légales dont la destination contient une
     * pièce adverse.
     *
     * <p>La prise en passant sera ajoutée lorsque le moteur complet exposera
     * explicitement cette information.</p>
     */
    public static Situation<CaptureDetection> captureAvailable() {
        return context -> {
            Analysis analysis = context.analysis();
            Color opponent = context.myColor().opposite();
            List<CaptureDetection> detections = new ArrayList<>();

            for (Move move : analysis.captures()) {
                Optional<Piece> attackerPiece =
                    context.position().pieceAt(move.from());
                Optional<Piece> targetPiece =
                    context.position().pieceAt(move.to());

                if (attackerPiece.isEmpty() || targetPiece.isEmpty()) {
                    continue;
                }

                Piece attacker = attackerPiece.orElseThrow();
                Piece target = targetPiece.orElseThrow();

                if (attacker.color() != context.myColor()
                    || target.color() != opponent) {
                    continue;
                }

                PlacedPiece attackerPlaced =
                    new PlacedPiece(attacker, move.from());
                PlacedPiece targetPlaced =
                    new PlacedPiece(target, move.to());

                detections.add(
                    new CaptureDetection(
                        move,
                        attackerPlaced,
                        targetPlaced,
                        analysis.pieceValues().valueOf(attacker.type()),
                        analysis.pieceValues().valueOf(target.type()),
                        analysis.attackersOf(move.to(), opponent).size()
                    )
                );
            }

            return List.copyOf(detections);
        };
    }

    /**
     * Détecte les captures d'une pièce adverse attaquée mais non défendue.
     *
     * <p>Cette définition est volontairement simple et lisible : une pièce est
     * ici dite "pendue" lorsque sa case est attaquée et qu'aucune autre pièce
     * de son camp ne défend cette case.</p>
     */
    public static Situation<CaptureDetection> hangingEnemyPiece() {
        return captureAvailable().filter(CaptureDetection::targetIsHanging);
    }
}
