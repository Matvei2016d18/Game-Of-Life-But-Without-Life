import javax.swing.*;
import java.awt.*;
import java.awt.event.*;

public class GOLBWL {
    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> new Game());
    }
}

class Game extends JFrame {
    private GamePanel gamePanel;
    
    public Game() {
        setTitle("Game of life but without life");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(600, 600);
        
        gamePanel = new GamePanel();
        add(gamePanel);
        
        setVisible(true);
    }
    
    class GamePanel extends JPanel {
        private String[][] grid;
        private String[][] nextGrid;
        private int rows = 18;
        private int cols = 20;
        private int cellSize = 29;
        private javax.swing.Timer timer;
        private boolean isRunning = false;
        private String selectedType = "g->";
        
        // Mouse drag state
        private boolean isLeftDragging = false;
        private boolean isRightDragging = false;
        
        // Colors
        private final Color COLOR_EMPTY = Color.BLACK;
        private final Color COLOR_GENERATOR = new Color(0, 200, 0);
        private final Color COLOR_MOVER = new Color(0, 150, 255);
        private final Color COLOR_BLOCK = new Color(100, 100, 100);
        private final Color COLOR_BARRIER = new Color(255, 0, 0);
        
        // Direction selection dialog
        private JDialog directionDialog;
        private String pendingType = null;
        
        public GamePanel() {
            setBackground(Color.DARK_GRAY);
            setPreferredSize(new Dimension(1200, 900));
            setLayout(new BorderLayout());
            
            initGrid();
            setupExampleLevel();
            
            // Create toolbar
            JPanel toolbar = new JPanel();
            toolbar.setBackground(Color.LIGHT_GRAY);
            
            // Control buttons
            JButton btnStart = new JButton("Start");
            JButton btnStop = new JButton("Stop");
            JButton btnStep = new JButton("Step");
            JButton btnClear = new JButton("Clear");
            JButton btnResize = new JButton("Resize Grid");
            
            // Blocks button with dropdown
            JButton btnBlocks = new JButton("Blocks");
            JPopupMenu blocksMenu = new JPopupMenu();
            
            JMenuItem itemGenerator = new JMenuItem("Generator");
            JMenuItem itemMover = new JMenuItem("Mover");
            JMenuItem itemBlock = new JMenuItem("Block");
            JMenuItem itemBarrier = new JMenuItem("Barrier");
            
            itemGenerator.addActionListener(e -> showDirectionDialog("g"));
            itemMover.addActionListener(e -> showDirectionDialog("m"));
            itemBlock.addActionListener(e -> { selectedType = "b"; setCursor(new Cursor(Cursor.CROSSHAIR_CURSOR)); });
            itemBarrier.addActionListener(e -> { selectedType = "bar"; setCursor(new Cursor(Cursor.CROSSHAIR_CURSOR)); });
            
            blocksMenu.add(itemGenerator);
            blocksMenu.add(itemMover);
            blocksMenu.add(itemBlock);
            blocksMenu.add(itemBarrier);
            
            btnBlocks.addActionListener(e -> blocksMenu.show(btnBlocks, 0, btnBlocks.getHeight()));
            
            btnStart.addActionListener(e -> startSimulation());
            btnStop.addActionListener(e -> stopSimulation());
            btnStep.addActionListener(e -> stepSimulation());
            btnClear.addActionListener(e -> clearGrid());
            btnResize.addActionListener(e -> showResizeDialog());
            
            toolbar.add(btnStart);
            toolbar.add(btnStop);
            toolbar.add(btnStep);
            toolbar.add(btnClear);
            toolbar.add(btnResize);
            toolbar.add(btnBlocks);
            
            add(toolbar, BorderLayout.NORTH);
            
            // Grid panel
            JPanel gridPanel = new JPanel() {
                @Override
                protected void paintComponent(Graphics g) {
                    super.paintComponent(g);
                    drawGrid(g);
                }
            };
            gridPanel.setBackground(Color.DARK_GRAY);
            gridPanel.setPreferredSize(new Dimension(cols * cellSize, rows * cellSize));
            
            // Mouse listener for clicks and dragging
            MouseAdapter mouseAdapter = new MouseAdapter() {
                public void mousePressed(MouseEvent e) {
                    if (SwingUtilities.isLeftMouseButton(e)) {
                        isLeftDragging = true;
                        placeCell(e);
                    } else if (SwingUtilities.isRightMouseButton(e)) {
                        isRightDragging = true;
                        removeCell(e);
                    }
                }
                
                public void mouseReleased(MouseEvent e) {
                    isLeftDragging = false;
                    isRightDragging = false;
                }
                
                public void mouseDragged(MouseEvent e) {
                    if (isLeftDragging) {
                        placeCell(e);
                    } else if (isRightDragging) {
                        removeCell(e);
                    }
                }
            };
            
            gridPanel.addMouseListener(mouseAdapter);
            gridPanel.addMouseMotionListener(mouseAdapter);
            
            add(gridPanel, BorderLayout.CENTER);
            setFocusable(true);
        }
        
        private void placeCell(MouseEvent e) {
            int col = e.getX() / cellSize;
            int row = e.getY() / cellSize;
            if (row >= 0 && row < rows && col >= 0 && col < cols) {
                grid[row][col] = selectedType;
                repaint();
            }
        }
        
        private void removeCell(MouseEvent e) {
            int col = e.getX() / cellSize;
            int row = e.getY() / cellSize;
            if (row >= 0 && row < rows && col >= 0 && col < cols) {
                grid[row][col] = "0";
                repaint();
            }
        }
        
        private void showDirectionDialog(String type) {
            if (directionDialog != null) {
                directionDialog.dispose();
            }
            
            directionDialog = new JDialog((Frame) SwingUtilities.getWindowAncestor(this), "Select Direction", true);
            directionDialog.setSize(300, 200);
            directionDialog.setLocationRelativeTo(this);
            directionDialog.setLayout(new GridLayout(2, 2, 10, 10));
            
            JButton btnUp = new JButton("^ Up");
            JButton btnDown = new JButton("v Down");
            JButton btnLeft = new JButton("< Left");
            JButton btnRight = new JButton("> Right");
            
            Font bigFont = new Font("Arial", Font.BOLD, 20);
            btnUp.setFont(bigFont);
            btnDown.setFont(bigFont);
            btnLeft.setFont(bigFont);
            btnRight.setFont(bigFont);
            
            btnUp.addActionListener(e -> {
                selectedType = type + "^";
                pendingType = null;
                directionDialog.dispose();
                setCursor(new Cursor(Cursor.CROSSHAIR_CURSOR));
            });
            btnDown.addActionListener(e -> {
                selectedType = type + "v";
                pendingType = null;
                directionDialog.dispose();
                setCursor(new Cursor(Cursor.CROSSHAIR_CURSOR));
            });
            btnLeft.addActionListener(e -> {
                selectedType = type + "<-";
                pendingType = null;
                directionDialog.dispose();
                setCursor(new Cursor(Cursor.CROSSHAIR_CURSOR));
            });
            btnRight.addActionListener(e -> {
                selectedType = type + "->";
                pendingType = null;
                directionDialog.dispose();
                setCursor(new Cursor(Cursor.CROSSHAIR_CURSOR));
            });
            
            directionDialog.add(btnUp);
            directionDialog.add(btnDown);
            directionDialog.add(btnLeft);
            directionDialog.add(btnRight);
            directionDialog.setVisible(true);
        }
        
        private void showResizeDialog() {
            JDialog resizeDialog = new JDialog((Frame) SwingUtilities.getWindowAncestor(this), "Resize Grid", true);
            resizeDialog.setSize(300, 200);
            resizeDialog.setLocationRelativeTo(this);
            resizeDialog.setLayout(new GridLayout(3, 2, 10, 10));
            
            resizeDialog.add(new JLabel("Rows (Y):"));
            JTextField rowsField = new JTextField(String.valueOf(rows));
            resizeDialog.add(rowsField);
            
            resizeDialog.add(new JLabel("Cols (X):"));
            JTextField colsField = new JTextField(String.valueOf(cols));
            resizeDialog.add(colsField);
            
            JButton btnApply = new JButton("Apply");
            JButton btnCancel = new JButton("Cancel");
            
            btnApply.addActionListener(e -> {
                try {
                    int newRows = Integer.parseInt(rowsField.getText());
                    int newCols = Integer.parseInt(colsField.getText());
                    if (newRows > 0 && newRows <= 50 && newCols > 0 && newCols <= 50) {
                        resizeGrid(newRows, newCols);
                    }
                } catch (NumberFormatException ex) {
                    JOptionPane.showMessageDialog(resizeDialog, "Please enter valid numbers");
                }
                resizeDialog.dispose();
            });
            
            btnCancel.addActionListener(e -> resizeDialog.dispose());
            
            resizeDialog.add(btnApply);
            resizeDialog.add(btnCancel);
            resizeDialog.setVisible(true);
        }
        
        private void resizeGrid(int newRows, int newCols) {
            String[][] newGrid = new String[newRows][newCols];
            for (int i = 0; i < newRows; i++) {
                for (int j = 0; j < newCols; j++) {
                    if (i < rows && j < cols) {
                        newGrid[i][j] = grid[i][j];
                    } else {
                        newGrid[i][j] = "0";
                    }
                }
            }
            
            rows = newRows;
            cols = newCols;
            grid = newGrid;
            nextGrid = new String[rows][cols];
            
            // Update UI
            JPanel parent = (JPanel) getComponent(1);
            parent.setPreferredSize(new Dimension(cols * cellSize, rows * cellSize));
            parent.revalidate();
            repaint();
        }
        
        private void initGrid() {
            grid = new String[rows][cols];
            nextGrid = new String[rows][cols];
            for (int i = 0; i < rows; i++) {
                for (int j = 0; j < cols; j++) {
                    grid[i][j] = "0";
                }
            }
        }
        
        private void setupExampleLevel() {
            if (rows > 15 && cols > 15) {
                grid[10][10] = "bar";
                grid[10][11] = "g->";
                grid[10][12] = "b";
            }
        }
        
        private void restartLevel() {
            stopSimulation();
            clearGrid();
            setupExampleLevel();
            repaint();
        }
        
        private void clearGrid() {
            for (int i = 0; i < rows; i++) {
                for (int j = 0; j < cols; j++) {
                    grid[i][j] = "0";
                }
            }
            repaint();
        }
        
        private void startSimulation() {
            if (timer == null) {
                timer = new javax.swing.Timer(300, e -> stepSimulation());
                timer.start();
                isRunning = true;
            }
        }
        
        private void stopSimulation() {
            if (timer != null) {
                timer.stop();
                timer = null;
                isRunning = false;
            }
        }
        
        private void stepSimulation() {
            updateGrid();
            repaint();
        }
        
        private void updateGrid() {
            for (int i = 0; i < rows; i++) {
                for (int j = 0; j < cols; j++) {
                    nextGrid[i][j] = grid[i][j];
                }
            }
            
            for (int i = 0; i < rows; i++) {
                for (int j = 0; j < cols; j++) {
                    String cell = grid[i][j];
                    
                    if (cell.startsWith("g")) {
                        processGenerator(i, j, cell);
                    } else if (cell.startsWith("m")) {
                        processMover(i, j, cell);
                    }
                }
            }
            
            for (int i = 0; i < rows; i++) {
                for (int j = 0; j < cols; j++) {
                    grid[i][j] = nextGrid[i][j];
                }
            }
        }
        
        private void processGenerator(int row, int col, String cell) {
            String dir = cell.substring(1);
            int targetRow = row, targetCol = col;
            int backRow = row, backCol = col;
            
            switch(dir) {
                case "->": targetCol = col + 1; backCol = col - 1; break;
                case "^": targetRow = row - 1; backRow = row + 1; break;
                case "v": targetRow = row + 1; backRow = row - 1; break;
                case "<-": targetCol = col - 1; backCol = col + 1; break;
            }
            
            if (targetRow >= 0 && targetRow < rows && targetCol >= 0 && targetCol < cols) {
                if (backRow >= 0 && backRow < rows && backCol >= 0 && backCol < cols) {
                    String backCell = grid[backRow][backCol];
                    
                    if (!backCell.equals("0")) {
                        if (nextGrid[targetRow][targetCol].equals("0")) {
                            nextGrid[targetRow][targetCol] = backCell;
                        } else if (!nextGrid[targetRow][targetCol].equals("bar")) {
                            int pushRow = targetRow, pushCol = targetCol;
                            
                            while (pushRow >= 0 && pushRow < rows && pushCol >= 0 && pushCol < cols && 
                                   !nextGrid[pushRow][pushCol].equals("0") && !nextGrid[pushRow][pushCol].equals("bar")) {
                                if (dir.equals("->")) pushCol++;
                                else if (dir.equals("<-")) pushCol--;
                                else if (dir.equals("^")) pushRow--;
                                else if (dir.equals("v")) pushRow++;
                            }
                            
                            if (pushRow >= 0 && pushRow < rows && pushCol >= 0 && pushCol < cols && 
                                nextGrid[pushRow][pushCol].equals("0")) {
                                
                                int currentRow = targetRow, currentCol = targetCol;
                                String currentCell = nextGrid[currentRow][currentCol];
                                
                                while (!(currentRow == pushRow && currentCol == pushCol)) {
                                    int nextRow = currentRow, nextCol = currentCol;
                                    if (dir.equals("->")) nextCol++;
                                    else if (dir.equals("<-")) nextCol--;
                                    else if (dir.equals("^")) nextRow--;
                                    else if (dir.equals("v")) nextRow++;
                                    
                                    nextGrid[nextRow][nextCol] = currentCell;
                                    currentCell = nextGrid[nextRow][nextCol];
                                    currentRow = nextRow;
                                    currentCol = nextCol;
                                }
                                
                                nextGrid[targetRow][targetCol] = backCell;
                            }
                        }
                    }
                }
            }
        }
        
        private void processMover(int row, int col, String cell) {
            String dir = cell.substring(1);
            int targetRow = row, targetCol = col;
            
            switch(dir) {
                case "->": targetCol = col + 1; break;
                case "^": targetRow = row - 1; break;
                case "v": targetRow = row + 1; break;
                case "<-": targetCol = col - 1; break;
            }
            
            if (targetRow >= 0 && targetRow < rows && targetCol >= 0 && targetCol < cols) {
                String targetCell = grid[targetRow][targetCol];
                
                if (targetCell.equals("0")) {
                    nextGrid[row][col] = "0";
                    nextGrid[targetRow][targetCol] = cell;
                } else if (!targetCell.equals("0") && !targetCell.equals("bar")) {
                    int pushRow = targetRow, pushCol = targetCol;
                    
                    while (pushRow >= 0 && pushRow < rows && pushCol >= 0 && pushCol < cols && 
                           !grid[pushRow][pushCol].equals("0") && !grid[pushRow][pushCol].equals("bar")) {
                        if (dir.equals("->")) pushCol++;
                        else if (dir.equals("<-")) pushCol--;
                        else if (dir.equals("^")) pushRow--;
                        else if (dir.equals("v")) pushRow++;
                    }
                    
                    if (pushRow >= 0 && pushRow < rows && pushCol >= 0 && pushCol < cols && 
                        grid[pushRow][pushCol].equals("0")) {
                        
                        int currentRow = targetRow, currentCol = targetCol;
                        String currentCell = grid[currentRow][currentCol];
                        
                        while (!(currentRow == pushRow && currentCol == pushCol)) {
                            int nextRow = currentRow, nextCol = currentCol;
                            if (dir.equals("->")) nextCol++;
                            else if (dir.equals("<-")) nextCol--;
                            else if (dir.equals("^")) nextRow--;
                            else if (dir.equals("v")) nextRow++;
                            
                            nextGrid[nextRow][nextCol] = currentCell;
                            currentCell = nextGrid[nextRow][nextCol];
                            currentRow = nextRow;
                            currentCol = nextCol;
                        }
                        
                        nextGrid[row][col] = "0";
                        nextGrid[targetRow][targetCol] = cell;
                    }
                }
            }
        }
        
        private void drawGrid(Graphics g) {
            Graphics2D g2d = (Graphics2D) g;
            
            for (int i = 0; i < rows; i++) {
                for (int j = 0; j < cols; j++) {
                    int x = j * cellSize;
                    int y = i * cellSize;
                    String cell = grid[i][j];
                    
                    if (cell.equals("0")) {
                        g2d.setColor(COLOR_EMPTY);
                        g2d.fillRect(x, y, cellSize - 1, cellSize - 1);
                    } else if (cell.equals("b")) {
                        g2d.setColor(COLOR_BLOCK);
                        g2d.fillRect(x, y, cellSize - 1, cellSize - 1);
                        g2d.setColor(Color.WHITE);
                        g2d.fillRect(x + cellSize/4, y + cellSize/4, cellSize/2, cellSize/2);
                    } else if (cell.equals("bar")) {
                        g2d.setColor(COLOR_BARRIER);
                        g2d.fillRect(x, y, cellSize - 1, cellSize - 1);
                        g2d.setColor(Color.WHITE);
                        int[] xPoints = {x + cellSize/2, x + cellSize - cellSize/4, x + cellSize/2, x + cellSize/4};
                        int[] yPoints = {y + cellSize/4, y + cellSize/2, y + cellSize - cellSize/4, y + cellSize/2};
                        g2d.fillPolygon(xPoints, yPoints, 4);
                    } else if (cell.startsWith("g")) {
                        g2d.setColor(COLOR_GENERATOR);
                        g2d.fillRect(x, y, cellSize - 1, cellSize - 1);
                        g2d.setColor(Color.WHITE);
                        g2d.setFont(new Font("Arial", Font.BOLD, cellSize / 2));
                        String text = "G";
                        FontMetrics fm = g2d.getFontMetrics();
                        int textX = x + (cellSize - fm.stringWidth(text)) / 2;
                        int textY = y + (cellSize + fm.getAscent() - fm.getDescent()) / 2;
                        g2d.drawString(text, textX, textY);
                        
                        String arrow = cell.substring(1);
                        g2d.setFont(new Font("Arial", Font.BOLD, cellSize / 3));
                        g2d.drawString(arrow.equals("->") ? ">" : arrow.equals("<-") ? "<" : arrow.equals("^") ? "^" : "v", 
                                      x + cellSize - cellSize/4, y + cellSize/4);
                    } else if (cell.startsWith("m")) {
                        g2d.setColor(COLOR_MOVER);
                        g2d.fillRect(x, y, cellSize - 1, cellSize - 1);
                        g2d.setColor(Color.WHITE);
                        g2d.setFont(new Font("Arial", Font.BOLD, cellSize / 2));
                        String text = "M";
                        FontMetrics fm = g2d.getFontMetrics();
                        int textX = x + (cellSize - fm.stringWidth(text)) / 2;
                        int textY = y + (cellSize + fm.getAscent() - fm.getDescent()) / 2;
                        g2d.drawString(text, textX, textY);
                        
                        String arrow = cell.substring(1);
                        g2d.setFont(new Font("Arial", Font.BOLD, cellSize / 3));
                        g2d.drawString(arrow.equals("->") ? ">" : arrow.equals("<-") ? "<" : arrow.equals("^") ? "^" : "v", 
                                      x + cellSize - cellSize/4, y + cellSize/4);
                    }
                    
                    g2d.setColor(Color.GRAY);
                    g2d.drawRect(x, y, cellSize - 1, cellSize - 1);
                }
            }
            
            // Draw status text
            g2d.setColor(Color.WHITE);
            g2d.setFont(new Font("Arial", Font.BOLD, 12));
            g2d.drawString("Selected: " + selectedType, 10, 20);
            if (isRunning) {
                g2d.drawString("* RUNNING", getWidth() - 100, 20);
            }
            g2d.drawString("Hold left button: place | Hold right button: delete", 10, getHeight() - 10);
        }
    }
}