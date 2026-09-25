package fr.astroware.chess.tournament.ui;

import fr.astroware.chess.bot.rule.AttemptStatus;
import fr.astroware.chess.bot.rule.RuleAttempt;
import fr.astroware.chess.core.model.BoardFile;
import fr.astroware.chess.core.model.BoardRank;
import fr.astroware.chess.core.model.Color;
import fr.astroware.chess.core.model.Piece;
import fr.astroware.chess.core.model.PieceType;
import fr.astroware.chess.core.model.PositionView;
import fr.astroware.chess.core.model.Square;
import fr.astroware.chess.core.rules.ChessRulesEngine;
import fr.astroware.chess.core.rules.ChessRulesEngines;
import fr.astroware.chess.tournament.match.MatchResult;
import fr.astroware.chess.tournament.match.PlayedMove;

import javax.swing.BorderFactory;
import javax.swing.DefaultListModel;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSplitPane;
import javax.swing.JTextArea;
import javax.swing.ListSelectionModel;
import javax.swing.SwingConstants;
import javax.swing.SwingUtilities;
import javax.swing.Timer;
import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.GridLayout;
import java.util.Objects;

/**
 * Viewer Swing d'une partie terminée.
 *
 * <p>Le viewer permet de naviguer coup par coup, de lancer une lecture
 * automatique et d'inspecter la règle ayant produit chaque décision.</p>
 */
public final class SwingMatchViewer {

    private SwingMatchViewer() {
    }

    public static void show(MatchResult result) {
        Objects.requireNonNull(result, "result must not be null");

        SwingUtilities.invokeLater(() ->
            new ViewerFrame(result).setVisible(true)
        );
    }

    private static final class ViewerFrame extends JFrame {

        private static final int BOARD_SIZE = 8;
        private static final Font PIECE_FONT =
            new Font(Font.SANS_SERIF, Font.PLAIN, 44);

        private final MatchResult result;
        private final ChessRulesEngine engine = ChessRulesEngines.standard();
        private final JLabel[][] squares =
            new JLabel[BOARD_SIZE][BOARD_SIZE];
        private final JTextArea details = new JTextArea();
        private final JLabel status = new JLabel();
        private final JList<String> moveList = new JList<>();
        private final DefaultListModel<String> moveModel =
            new DefaultListModel<>();
        private final Timer autoplay;

        private int positionIndex = 0;
        private boolean flipped = false;

        ViewerFrame(MatchResult result) {
            super(
                result.white().botName()
                    + " vs "
                    + result.black().botName()
                    + " — Chess Framework"
            );

            this.result = result;
            this.autoplay = new Timer(900, event -> stepForward());

            setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
            setLayout(new BorderLayout(8, 8));
            setMinimumSize(new Dimension(1050, 720));

            add(createHeader(), BorderLayout.NORTH);
            add(createMainContent(), BorderLayout.CENTER);
            add(createControls(), BorderLayout.SOUTH);

            populateMoves();
            render();

            pack();
            setLocationRelativeTo(null);
        }

        private JPanel createHeader() {
            JPanel panel = new JPanel(new BorderLayout());

            JLabel players = new JLabel(
                "<html><b>Blancs :</b> "
                    + escapeHtml(result.white().botName())
                    + " — "
                    + escapeHtml(result.white().authorName())
                    + "&nbsp;&nbsp;&nbsp;&nbsp;"
                    + "<b>Noirs :</b> "
                    + escapeHtml(result.black().botName())
                    + " — "
                    + escapeHtml(result.black().authorName())
                    + "</html>"
            );

            status.setHorizontalAlignment(SwingConstants.RIGHT);

            panel.add(players, BorderLayout.WEST);
            panel.add(status, BorderLayout.EAST);
            panel.setBorder(BorderFactory.createEmptyBorder(8, 8, 0, 8));

            return panel;
        }

        private JSplitPane createMainContent() {
            JPanel board = new JPanel(
                new GridLayout(BOARD_SIZE, BOARD_SIZE)
            );

            board.setPreferredSize(new Dimension(640, 640));

            for (int row = 0; row < BOARD_SIZE; row++) {
                for (int col = 0; col < BOARD_SIZE; col++) {
                    JLabel label = new JLabel(
                        "",
                        SwingConstants.CENTER
                    );
                    label.setOpaque(true);
                    label.setFont(PIECE_FONT);
                    label.setBorder(
                        BorderFactory.createLineBorder(
                            new java.awt.Color(90, 90, 90)
                        )
                    );

                    squares[row][col] = label;
                    board.add(label);
                }
            }

            JPanel side = new JPanel(new BorderLayout(6, 6));
            moveList.setModel(moveModel);
            moveList.setSelectionMode(
                ListSelectionModel.SINGLE_SELECTION
            );
            moveList.addListSelectionListener(event -> {
                if (!event.getValueIsAdjusting()
                    && moveList.getSelectedIndex() >= 0) {
                    positionIndex = moveList.getSelectedIndex() + 1;
                    render();
                }
            });

            details.setEditable(false);
            details.setLineWrap(true);
            details.setWrapStyleWord(true);
            details.setFont(
                new Font(Font.MONOSPACED, Font.PLAIN, 13)
            );

            JScrollPane movesScroll = new JScrollPane(moveList);
            movesScroll.setPreferredSize(new Dimension(330, 300));

            JScrollPane detailsScroll = new JScrollPane(details);
            detailsScroll.setPreferredSize(new Dimension(330, 300));

            JSplitPane sideSplit = new JSplitPane(
                JSplitPane.VERTICAL_SPLIT,
                movesScroll,
                detailsScroll
            );
            sideSplit.setResizeWeight(0.5);

            side.add(sideSplit, BorderLayout.CENTER);

            JSplitPane main = new JSplitPane(
                JSplitPane.HORIZONTAL_SPLIT,
                board,
                side
            );
            main.setResizeWeight(0.68);
            main.setBorder(BorderFactory.createEmptyBorder(0, 8, 0, 8));

            return main;
        }

        private JPanel createControls() {
            JPanel controls = new JPanel();

            JButton first = new JButton("|<");
            JButton previous = new JButton("<");
            JButton play = new JButton("Lecture");
            JButton next = new JButton(">");
            JButton last = new JButton(">|");
            JButton flip = new JButton("Retourner");

            first.addActionListener(event -> {
                autoplay.stop();
                positionIndex = 0;
                render();
            });

            previous.addActionListener(event -> {
                autoplay.stop();
                if (positionIndex > 0) {
                    positionIndex--;
                    render();
                }
            });

            play.addActionListener(event -> {
                if (autoplay.isRunning()) {
                    autoplay.stop();
                    play.setText("Lecture");
                } else {
                    if (positionIndex >= result.playedMoves().size()) {
                        positionIndex = 0;
                    }
                    autoplay.start();
                    play.setText("Pause");
                }
            });

            next.addActionListener(event -> {
                autoplay.stop();
                stepForward();
            });

            last.addActionListener(event -> {
                autoplay.stop();
                positionIndex = result.playedMoves().size();
                render();
            });

            flip.addActionListener(event -> {
                flipped = !flipped;
                render();
            });

            controls.add(first);
            controls.add(previous);
            controls.add(play);
            controls.add(next);
            controls.add(last);
            controls.add(flip);

            return controls;
        }

        private void populateMoves() {
            for (PlayedMove move : result.playedMoves()) {
                String prefix = move.color() == Color.WHITE
                    ? move.fullMoveNumber() + "."
                    : move.fullMoveNumber() + "...";

                moveModel.addElement(
                    prefix
                        + " "
                        + move.san()
                        + " — "
                        + move.bot().botName()
                        + " ("
                        + String.format(
                            java.util.Locale.ROOT,
                            "%.1f ms",
                            move.decisionMillis()
                        )
                        + ")"
                );
            }
        }

        private void stepForward() {
            if (positionIndex < result.playedMoves().size()) {
                positionIndex++;
                render();
            } else {
                autoplay.stop();
            }
        }

        private void render() {
            String fen = positionIndex == 0
                ? result.initialFen()
                : result.playedMoves()
                    .get(positionIndex - 1)
                    .afterFen();

            PositionView position = engine.fromFen(fen);

            for (int displayRow = 0; displayRow < BOARD_SIZE; displayRow++) {
                for (
                    int displayCol = 0;
                    displayCol < BOARD_SIZE;
                    displayCol++
                ) {
                    int fileIndex = flipped
                        ? BOARD_SIZE - 1 - displayCol
                        : displayCol;

                    int rankNumber = flipped
                        ? displayRow + 1
                        : BOARD_SIZE - displayRow;

                    Square square = new Square(
                        BoardFile.values()[fileIndex],
                        BoardRank.from(rankNumber)
                    );

                    JLabel cell = squares[displayRow][displayCol];
                    Piece piece = position.pieceAt(square).orElse(null);

                    cell.setText(piece == null ? "" : symbol(piece));

                    boolean light =
                        (fileIndex + rankNumber) % 2 == 1;

                    java.awt.Color baseColor =
                        light
                            ? new java.awt.Color(238, 238, 210)
                            : new java.awt.Color(118, 150, 86);

                    if (positionIndex > 0) {
                        PlayedMove last =
                            result.playedMoves().get(positionIndex - 1);

                        if (square.equals(last.decision().move().from())
                            || square.equals(last.decision().move().to())) {
                            baseColor = new java.awt.Color(246, 246, 105);
                        }
                    }

                    cell.setBackground(baseColor);
                    cell.setToolTipText(square.notation());
                }
            }

            if (positionIndex == 0) {
                moveList.clearSelection();
                details.setText(
                    "Position initiale\n\nFEN :\n"
                        + result.initialFen()
                );
            } else {
                PlayedMove move =
                    result.playedMoves().get(positionIndex - 1);
                moveList.setSelectedIndex(positionIndex - 1);
                moveList.ensureIndexIsVisible(positionIndex - 1);
                details.setText(detailsFor(move));
            }

            status.setText(
                "Position "
                    + positionIndex
                    + "/"
                    + result.playedMoves().size()
                    + " — résultat "
                    + result.pgnResult()
            );
        }

        private String detailsFor(PlayedMove move) {
            StringBuilder text = new StringBuilder();

            text.append("Coup : ")
                .append(move.san())
                .append(" (")
                .append(move.decision().move().toUci())
                .append(")\n");

            text.append("Bot : ")
                .append(move.bot().botName())
                .append("\n");

            text.append("Temps : ")
                .append(
                    String.format(
                        java.util.Locale.ROOT,
                        "%.2f ms",
                        move.decisionMillis()
                    )
                )
                .append("\n");

            move.decision().trace().stream()
                .filter(attempt ->
                    attempt.status() == AttemptStatus.SELECTED
                )
                .findFirst()
                .ifPresent(attempt -> appendAttempt(text, attempt));

            text.append("\nFEN après :\n")
                .append(move.afterFen());

            return text.toString();
        }

        private static void appendAttempt(
            StringBuilder text,
            RuleAttempt attempt
        ) {
            text.append("Règle : ")
                .append(attempt.ruleName())
                .append("\n");

            attempt.selectedMove().ifPresent(candidate -> {
                text.append("Score : ")
                    .append(String.format("%.2f", candidate.score().value()))
                    .append("/10\n");

                text.append("Agressivité : ")
                    .append(candidate.aggression().value())
                    .append("/10\n");

                text.append("Sécurité : ")
                    .append(candidate.safety().value())
                    .append("/10\n");

                text.append("Risque : ")
                    .append(candidate.risk().value())
                    .append("/10\n");

                if (!candidate.explanation().isBlank()) {
                    text.append("\n")
                        .append(candidate.explanation())
                        .append("\n");
                }
            });
        }

        @Override
        public void dispose() {
            autoplay.stop();
            super.dispose();
        }

        private static String symbol(Piece piece) {
            return switch (piece.color()) {
                case WHITE -> switch (piece.type()) {
                    case KING -> "♔";
                    case QUEEN -> "♕";
                    case ROOK -> "♖";
                    case BISHOP -> "♗";
                    case KNIGHT -> "♘";
                    case PAWN -> "♙";
                };
                case BLACK -> switch (piece.type()) {
                    case KING -> "♚";
                    case QUEEN -> "♛";
                    case ROOK -> "♜";
                    case BISHOP -> "♝";
                    case KNIGHT -> "♞";
                    case PAWN -> "♟";
                };
            };
        }

        private static String escapeHtml(String value) {
            return value
                .replace("&", "&amp;")
                .replace("<", "&lt;")
                .replace(">", "&gt;");
        }
    }
}
