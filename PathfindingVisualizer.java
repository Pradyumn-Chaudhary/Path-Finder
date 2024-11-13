import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.LinkedList;
import java.util.Queue;

public class PathfindingVisualizer {
    private static final int ROWS = 20;
    private static final int COLS = 20;
    private static final int CELL_SIZE = 30;
    private static final Color WALL_COLOR = Color.BLACK;
    private static final Color EMPTY_COLOR = Color.WHITE;
    private static final Color VISITED_COLOR = Color.CYAN;
    private static final Color START_COLOR = new Color(76, 175, 80);
    private static final Color END_COLOR = new Color(244, 67, 54);

    private JButton startCell = null;
    private JButton endCell = null;
    private JButton[][] cells = new JButton[ROWS][COLS];

    public static void main(String[] args) {
        new PathfindingVisualizer().createGUI();
    }

    private void createGUI() {
        JFrame frame = new JFrame("Pathfinding Visualizer");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setSize(COLS * CELL_SIZE, ROWS * CELL_SIZE + 50);
        frame.setLocationRelativeTo(null);

        JPanel gridPanel = new JPanel(new GridLayout(ROWS, COLS));
        JPanel controlPanel = new JPanel();
        JButton startButton = new JButton("Start BFS");
        JButton clearButton = new JButton("Clear Grid");

        startButton.addActionListener(e -> startBFS());
        clearButton.addActionListener(e -> clearGrid());

        controlPanel.add(startButton);
        controlPanel.add(clearButton);

        for (int i = 0; i < ROWS; i++) {
            for (int j = 0; j < COLS; j++) {
                JButton cell = new JButton();
                cell.setPreferredSize(new Dimension(CELL_SIZE, CELL_SIZE));
                cell.setBackground(EMPTY_COLOR);
                cell.setBorder(BorderFactory.createLineBorder(Color.GRAY));
                cell.addActionListener(e -> handleCellClick(cell));
                cells[i][j] = cell;
                gridPanel.add(cell);
            }
        }

        frame.add(gridPanel, BorderLayout.CENTER);
        frame.add(controlPanel, BorderLayout.SOUTH);
        frame.setVisible(true);
    }

    private void handleCellClick(JButton cell) {
        if (startCell == null) {
            startCell = cell;
            startCell.setText("🚶"); // Start point
            startCell.setFont(new Font("Arial", Font.PLAIN, 18));
        } else if (endCell == null && cell != startCell) {
            endCell = cell;
            endCell.setText("🏠"); // End point
            endCell.setFont(new Font("Arial", Font.PLAIN, 18));
        } else if (cell != startCell && cell != endCell) {
            if (cell.getBackground() == WALL_COLOR) {
                cell.setBackground(EMPTY_COLOR);
                cell.setText("");
            } else {
                cell.setBackground(WALL_COLOR);
            }
        }
    }

    private void clearGrid() {
        for (int i = 0; i < ROWS; i++) {
            for (int j = 0; j < COLS; j++) {
                cells[i][j].setBackground(EMPTY_COLOR);
                cells[i][j].setText("");
            }
        }
        startCell = null;
        endCell = null;
    }

    private void startBFS() {
        if (startCell == null || endCell == null) {
            JOptionPane.showMessageDialog(null, "Please set start and end points.");
            return;
        }

        int[] startCoords = getCellCoordinates(startCell);
        int[] endCoords = getCellCoordinates(endCell);
        boolean[][] visited = new boolean[ROWS][COLS];
        Point[][] prev = new Point[ROWS][COLS]; // Store predecessors for path reconstruction
        Queue<Point> queue = new LinkedList<>();

        queue.add(new Point(startCoords[0], startCoords[1]));
        visited[startCoords[0]][startCoords[1]] = true;

        Timer timer = new Timer(10, null);
        timer.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                if (queue.isEmpty()) {
                    timer.stop();
                    JOptionPane.showMessageDialog(null, "No path found.");
                    return;
                }

                Point current = queue.poll();
                int x = current.x;
                int y = current.y;

                if (x == endCoords[0] && y == endCoords[1]) {
                    timer.stop();
                    animatePath(prev, startCoords, endCoords); // Animate path reconstruction
                    return;
                }

                for (int[] direction : new int[][]{{1, 0}, {-1, 0}, {0, 1}, {0, -1}}) {
                    int newX = x + direction[0];
                    int newY = y + direction[1];

                    if (isValidMove(newX, newY, visited)) {
                        queue.add(new Point(newX, newY));
                        visited[newX][newY] = true;
                        prev[newX][newY] = current; // Record predecessor
                        cells[newX][newY].setBackground(VISITED_COLOR); // Mark as visited
                    }
                }
            }
        });
        timer.start();
    }

    private boolean isValidMove(int x, int y, boolean[][] visited) {
        return x >= 0 && y >= 0 && x < ROWS && y < COLS && !visited[x][y] && cells[x][y].getBackground() != WALL_COLOR;
    }

    private void animatePath(Point[][] prev, int[] start, int[] end) {
        // Start from the end and trace the path backwards
        Point current = new Point(end[0], end[1]);

        // Ensure there is a valid path
        if (prev[current.x][current.y] == null && (current.x != start[0] || current.y != start[1])) {
            JOptionPane.showMessageDialog(null, "No path found.");
            return;
        }

        // Color gradient for dynamic path (Shades of purple)
        int[] gradient = {0}; // Gradient counter for color change

        // Trace the path and highlight cells dynamically
        while (current.x != start[0] || current.y != start[1]) {
            // Create a shade of purple by adjusting the green component
            int r = 128; // Red component (stay constant)
            int g = (gradient[0] % 256); // Vary the green component for shades
            int b = 128; // Blue component (stay constant)

            // Set the background of each path cell with a shade of purple
            cells[current.x][current.y].setBackground(new Color(r, g, b));

            // Move to the predecessor cell
            Point prevCell = prev[current.x][current.y];
            if (prevCell != null) {
                current = prevCell; // Move to the predecessor
            } else {
                JOptionPane.showMessageDialog(null, "Error in path reconstruction.");
                return;
            }

            gradient[0] += 10; // Gradually adjust the green component for color change
        }

        // Finally, highlight the start and end cells with distinct colors
        cells[start[0]][start[1]].setBackground(Color.GREEN); // Start cell (green)
        cells[end[0]][end[1]].setBackground(Color.RED); // End cell (red)
    }

    private int[] getCellCoordinates(JButton cell) {
        for (int i = 0; i < ROWS; i++) {
            for (int j = 0; j < COLS; j++) {
                if (cells[i][j] == cell) {
                    return new int[]{i, j};
                }
            }
        }
        return null;
    }
}
