package fr.astroware.chess.bot.analysis;

import fr.astroware.chess.bot.api.BotContext;
import fr.astroware.chess.core.model.Color;
import fr.astroware.chess.core.model.Move;
import fr.astroware.chess.core.model.PlacedPiece;
import fr.astroware.chess.core.model.Square;

import java.util.List;

/**
 * Façade d'analyse de la position mise à disposition des bots.
 */
public interface Analysis {

    AttackMap attackMap();

    List<PlacedPiece> attackersOf(Square square, Color color);

    List<PlacedPiece> defendersOf(PlacedPiece piece);

    boolean isAttacked(PlacedPiece piece);

    boolean isDefended(PlacedPiece piece);

    boolean isHanging(PlacedPiece piece);

    List<PlacedPiece> hangingPieces(Color color);

    int material(Color color);

    MaterialBalance materialBalance();

    List<Move> captures();

    PieceValues pieceValues();

    /**
     * Structure de pions du camp demandé.
     */
    PawnStructure pawnStructure(Color color);

    /**
     * État d'une colonne selon la présence de pions.
     */
    FileStatus fileStatus(fr.astroware.chess.core.model.BoardFile file);

    /**
     * Activité / mobilité relative normalisée de 0 à 10.
     */
    double mobilityScore(Color color);

    /**
     * Contrôle du centre normalisé de 0 à 10.
     */
    double centerControlScore(Color color);

    /**
     * Sécurité du roi normalisée de 0 à 10.
     */
    double kingSafetyScore(Color color);

    /**
     * Évaluation positionnelle globale du point de vue demandé.
     */
    PositionEvaluation positionEvaluation(Color color);

    /**
     * Estimation heuristique de la grande phase de la partie.
     */
    GamePhase gamePhase();

    /**
     * Coups légaux du camp au trait qui terminent immédiatement la partie
     * par mat.
     */
    List<Move> mateInOneMoves();

    /**
     * Indique si le roi du camp au trait est actuellement en échec.
     */
    boolean isKingAttacked();

    /**
     * Clouages absolus exercés par le camp indiqué.
     */
    List<PinPattern> pinsBy(Color color);

    /**
     * Enfilades exercées par le camp indiqué.
     */
    List<SkewerPattern> skewersBy(Color color);

    /**
     * Défenseurs du camp indiqué qui sont l'unique défenseur d'au moins deux
     * pièces actuellement attaquées par le camp adverse.
     */
    List<OverloadedDefenderPattern> overloadedDefenders(Color color);

    /**
     * Simule un coup légal et retourne une nouvelle position entièrement
     * analysable, sans modifier la partie réelle.
     */
    PositionProjection after(Move move);

    static Analysis of(BotContext context) {
        return new DefaultAnalysis(context);
    }
}
