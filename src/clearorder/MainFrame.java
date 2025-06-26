package clearorder;

import java.awt.BorderLayout;
import java.awt.CardLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Container;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.Frame;
import java.awt.Graphics;
import java.awt.GridLayout;
import java.awt.Image;
import java.awt.Insets;
import java.awt.Window;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.text.SimpleDateFormat;
import java.util.List;

import javax.swing.AbstractCellEditor;
import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSpinner;
import javax.swing.JTable;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.JToggleButton;
import javax.swing.SpinnerDateModel;
import javax.swing.SwingConstants;
import javax.swing.SwingUtilities;
import javax.swing.table.AbstractTableModel;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.JTableHeader;
import javax.swing.table.TableCellEditor;
import javax.swing.table.TableCellRenderer;

import clearorder.dao.CustomerDAO;
import clearorder.dao.OrderDAO;
import clearorder.dao.ProductDAO;
import clearorder.dao.UserDAO;
import clearorder.model.User;

public class MainFrame extends JFrame {
        private CardLayout cardLayout;
        public JPanel mainPanel;
        private User currentUser;
        private Color primaryBlue = new Color(51, 122, 220);
        private Color sidebarBg = new Color(245, 245, 245);
        private Color hoverBg = new Color(235, 235, 235);

        public MainFrame(User user) {
                this.currentUser = user;
                setTitle("HighSpeed-Sales");
                setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
                setExtendedState(JFrame.MAXIMIZED_BOTH);
                setLocationRelativeTo(null);
                setLayout(new BorderLayout());

                // Top blue bar with app name
                JPanel topBar = new JPanel();
                topBar.setBackground(primaryBlue);
                topBar.setPreferredSize(new Dimension(0, 60));
                topBar.setBorder(BorderFactory.createMatteBorder(0, 0, 1, 0, new Color(45, 110, 200)));

                JLabel appName = new JLabel("HighSpeed Sales");
                appName.setForeground(Color.WHITE);
                appName.setFont(new Font("Arial", Font.BOLD, 24));
                topBar.setLayout(new FlowLayout(FlowLayout.LEFT, 30, 15));
                topBar.add(appName);
                add(topBar, BorderLayout.NORTH);

                // Sidebar with improved design
                JPanel sidebar = new JPanel();
                sidebar.setBackground(sidebarBg);
                sidebar.setLayout(new BoxLayout(sidebar, BoxLayout.Y_AXIS));
                sidebar.setPreferredSize(new Dimension(220, 0));
                sidebar.setBorder(BorderFactory.createMatteBorder(0, 0, 0, 1, new Color(220, 220, 220)));

                // Logo area
                JPanel logoPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 0, 0));
                logoPanel.setBackground(sidebarBg);
                logoPanel.setPreferredSize(new Dimension(220, 100));
                logoPanel.setMaximumSize(new Dimension(220, 100));

                java.net.URL logoUrl = getClass().getClassLoader().getResource("images/clearorder-neueLogo.png");
                JLabel logo;
                if (logoUrl != null) {
                    ImageIcon originalIcon = new ImageIcon(logoUrl);
                    Image scaledImage = originalIcon.getImage().getScaledInstance(220, 100, Image.SCALE_SMOOTH);
                    ImageIcon logoIcon = new ImageIcon(scaledImage);
                    logo = new JLabel(logoIcon);
                } else {
                    logo = new JLabel("HighSpeed-Logo");
                    logo.setFont(new Font("Arial", Font.BOLD, 18));
                    logo.setForeground(primaryBlue);
                }
                logo.setPreferredSize(new Dimension(220, 100));
                logo.setMaximumSize(new Dimension(220, 100));
                logo.setHorizontalAlignment(SwingConstants.CENTER);
                logoPanel.add(logo);
                sidebar.add(logoPanel);
                sidebar.add(Box.createVerticalStrut(20));

                // Navigation buttons
                String[] navItems = { "Dashboard", "Order", "Customers", "PC Builder" };
                if (currentUser.getDepartment() != null && currentUser.getDepartment().equals("Verkauf_Leitung")) {
                        navItems = new String[] { "Dashboard", "Order", "Customers", "PC Builder", "Users" };
                }

                for (String item : navItems) {
                        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 20, 0));
                        buttonPanel.setMaximumSize(new Dimension(220, 40));
                        buttonPanel.setBackground(sidebarBg);

                        JButton btn = new JButton(item);
                        btn.setFont(new Font("Arial", Font.PLAIN, 14));
                        btn.setForeground(new Color(60, 60, 60));
                        btn.setBorderPainted(false);
                        btn.setContentAreaFilled(false);
                        btn.setFocusPainted(false);
                        btn.setCursor(new Cursor(Cursor.HAND_CURSOR));
                        btn.addActionListener(e -> showPanel(item));

                        buttonPanel.addMouseListener(new MouseAdapter() {
                                public void mouseEntered(MouseEvent e) {
                                        buttonPanel.setBackground(hoverBg);
                                }

                                public void mouseExited(MouseEvent e) {
                                        buttonPanel.setBackground(sidebarBg);
                                }

                                public void mouseClicked(MouseEvent e) {
                                        showPanel(item);
                                }
                        });

                        buttonPanel.add(btn);
                        sidebar.add(buttonPanel);
                        sidebar.add(Box.createVerticalStrut(5));
                }

                sidebar.add(Box.createVerticalGlue());

                // User info panel at bottom
                JPanel userInfoPanel = new JPanel();
                userInfoPanel.setLayout(new BoxLayout(userInfoPanel, BoxLayout.Y_AXIS));
                userInfoPanel.setBackground(sidebarBg);
                userInfoPanel.setBorder(BorderFactory.createMatteBorder(1, 0, 0, 0, new Color(220, 220, 220)));
                userInfoPanel.setMaximumSize(new Dimension(220, 80));

                JLabel nameLabel = new JLabel(currentUser.getEmployeeName());
                nameLabel.setFont(new Font("Arial", Font.BOLD, 13));
                nameLabel.setAlignmentX(Component.CENTER_ALIGNMENT);

                JLabel emailLabel = new JLabel(currentUser.getEmployeeEmail());
                emailLabel.setFont(new Font("Arial", Font.PLAIN, 12));
                emailLabel.setForeground(new Color(100, 100, 100));
                emailLabel.setAlignmentX(Component.CENTER_ALIGNMENT);

                userInfoPanel.add(Box.createVerticalStrut(15));
                userInfoPanel.add(nameLabel);
                userInfoPanel.add(Box.createVerticalStrut(5));
                userInfoPanel.add(emailLabel);
                userInfoPanel.add(Box.createVerticalStrut(15));

                sidebar.add(userInfoPanel);
                add(sidebar, BorderLayout.WEST);

                // Main content area with CardLayout
                cardLayout = new CardLayout();
                mainPanel = new JPanel(cardLayout);
                mainPanel.setBackground(Color.WHITE);
                mainPanel.add(new DashboardPanel(), "Dashboard");
                mainPanel.add(new OrderPanel(), "Order");
                mainPanel.add(new CustomerPanel(), "Customers");
                mainPanel.add(new PCBuilderPanel(), "PC Builder");
                if (currentUser.getDepartment() != null && currentUser.getDepartment().equals("Verkauf_Leitung")) {
                        mainPanel.add(new UserListPanel(), "Users");
                }
                add(mainPanel, BorderLayout.CENTER);
                showPanel("Dashboard");
        }

        private void showPanel(String name) {
                cardLayout.show(mainPanel, name);
                // Refresh order data if switching to Order panel
                if ("Order".equals(name)) {
                    for (Component comp : mainPanel.getComponents()) {
                        if (comp instanceof OrderPanel && comp.isVisible()) {
                            ((OrderPanel) comp).loadOrderData();
                        }
                    }
                }
        }

        // Get the current displayed panel
        public Component getCurrentPanel() {
            for (Component comp : mainPanel.getComponents()) {
                if (comp.isVisible()) {
                    return comp;
                }
            }
            return null;
        }

        // Dashboard panel with action cards
        static class DashboardPanel extends JPanel {
                DashboardPanel() {
                        setLayout(new BorderLayout());
                        JPanel contentPanel = new JPanel(new BorderLayout());
                        contentPanel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));

                        // Welcome message
                        JPanel welcomePanel = new JPanel(new FlowLayout(FlowLayout.CENTER));
                        JLabel welcome = new JLabel("Welcome to HighSpeed-Sales!");
                        welcome.setFont(new Font("Arial", Font.BOLD, 24));
                        welcome.setForeground(new Color(51, 122, 220));
                        welcomePanel.add(welcome);
                        contentPanel.add(welcomePanel, BorderLayout.NORTH);

                        // Main cards panel
                        JPanel mainCardsPanel = new JPanel(new GridLayout(2, 2, 20, 20));
                        mainCardsPanel.setBorder(BorderFactory.createEmptyBorder(20, 0, 20, 0));

                        String[] mainCards = {
                                "ORDER LIST",
                                "CUSTOMER LIST",
                                "CREATE ORDER",
                                "ADD NEW CUSTOMER"
                        };
                        for (int i = 0; i < mainCards.length; i++) {
                                String cardText = mainCards[i];
                                JPanel cardPanel;
                                if (i == 0) { // Top left: Order List
                                        cardPanel = createCardPanel(cardText, true, "Order");
                                } else if (i == 1) { // Top right: Customer List
                                        cardPanel = createCardPanel(cardText, true, "Customers");
                                } else {
                                        cardPanel = createCardPanel(cardText, true, null);
                                }
                                mainCardsPanel.add(cardPanel);
                        }

                        // Logout button panel at the bottom
                        JPanel logoutPanel = new JPanel(new FlowLayout(FlowLayout.CENTER));
                        JPanel logoutCard = createCardPanel("LOGOUT", true, null);
                        logoutCard.setPreferredSize(new Dimension(200, 80));
                        logoutPanel.add(logoutCard);

                        // Add panels to content
                        JPanel centerPanel = new JPanel(new BorderLayout());
                        centerPanel.add(mainCardsPanel, BorderLayout.CENTER);
                        centerPanel.add(logoutPanel, BorderLayout.SOUTH);
                        contentPanel.add(centerPanel, BorderLayout.CENTER);

                        add(contentPanel);
                }

                private JPanel createCardPanel(String text, boolean isClickable, String tabToShow) {
                        JPanel cardPanel = new JPanel();
                        cardPanel.setLayout(new BoxLayout(cardPanel, BoxLayout.Y_AXIS));
                        cardPanel.setBackground(Color.WHITE);
                        cardPanel.setBorder(BorderFactory.createCompoundBorder(
                                        BorderFactory.createLineBorder(new Color(220, 220, 220)),
                                        BorderFactory.createEmptyBorder(20, 20, 20, 20)));

                        JLabel textLabel = new JLabel(text);
                        textLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
                        textLabel.setFont(new Font("Arial", Font.BOLD, 14));
                        textLabel.setForeground(new Color(51, 122, 220));
                        cardPanel.add(textLabel);

                        if (isClickable) {
                                cardPanel.setCursor(new Cursor(Cursor.HAND_CURSOR));
                                cardPanel.addMouseListener(new MouseAdapter() {
                                        public void mouseEntered(MouseEvent e) {
                                                cardPanel.setBackground(new Color(245, 245, 245));
                                        }

                                        public void mouseExited(MouseEvent e) {
                                                cardPanel.setBackground(Color.WHITE);
                                        }

                                        public void mouseClicked(MouseEvent e) {
                                                if (text.equals("LOGOUT")) {
                                                        // Show login dialog again
                                                        JFrame topFrame = (JFrame) SwingUtilities
                                                                        .getWindowAncestor(cardPanel);
                                                        topFrame.dispose();
                                                        SwingUtilities.invokeLater(() -> {
                                                                LoginDialog loginDialog = new LoginDialog();
                                                                loginDialog.setVisible(true);
                                                                if (loginDialog.isSucceeded()) {
                                                                        MainFrame frame = new MainFrame(
                                                                                        loginDialog.getLoggedInUser());
                                                                        frame.setVisible(true);
                                                                } else {
                                                                        System.exit(0);
                                                                }
                                                        });
                                                } else if (text.equals("CREATE ORDER")) {
                                                        // Open PC Builder Dialog instead of CreateOrderFrame
                                                        System.out.println("[DEBUG] Dashboard CREATE ORDER button clicked");
                                                        SwingUtilities.invokeLater(() -> {
                                                                try {
                                                                        System.out.println("[DEBUG] Creating PCBuilderDialog from Dashboard...");
                                                                        JFrame parentFrame = (JFrame) SwingUtilities.getWindowAncestor(DashboardPanel.this);
                                                                        System.out.println("[DEBUG] Parent frame: " + parentFrame);
                                                                        PCBuilderDialog dialog = new PCBuilderDialog(
                                                                                parentFrame,
                                                                                new OrderCreationListener() {
                                                                                        @Override
                                                                                        public void addCustomer(String id, String name, String email, String phone) {
                                                                                                // Add customer if needed
                                                                                        }
                                                                                                         @Override
                                                                        public void addOrder(String orderId, String orderNumber, String status, String customerName, 
                                                                                        String date, String invoiceId, java.util.List<PCBuilderPanel.ProductInfo> selectedProducts) {
                                                                                System.out.println("[DEBUG] MainFrame.addOrder called with orderId=" + orderId + ", products=" + (selectedProducts != null ? selectedProducts.size() : "null"));
                                                                                
                                                                                // Refresh the Order panel when the dialog is closed
                                                                                // This will be handled by the window listener in PCBuilderDialog
                                                                                
                                                                                // Verify that the order was actually saved by checking the database
                                                                                javax.swing.SwingUtilities.invokeLater(() -> {
                                                                                    try {
                                                                                        java.sql.Connection conn = clearorder.util.DatabaseConnection.getConnection();
                                                                                        String sql = "SELECT COUNT(*) as product_count FROM order_products WHERE order_id = ?";
                                                                                        java.sql.PreparedStatement stmt = conn.prepareStatement(sql);
                                                                                        stmt.setInt(1, Integer.parseInt(orderId));
                                                                                        java.sql.ResultSet rs = stmt.executeQuery();
                                                                                        
                                                                                        if (rs.next()) {
                                                                                            int productCount = rs.getInt("product_count");
                                                                                            System.out.println("[DEBUG] Verification: Order " + orderId + " has " + productCount + " products in database");
                                                                                            
                                                                                            if (productCount == 0) {
                                                                                                System.err.println("[ERROR] Order " + orderId + " was created but NO PRODUCTS were saved to order_products table!");
                                                                                            }
                                                                                        }
                                                                                        rs.close();
                                                                                        stmt.close();
                                                                                        conn.close();
                                                                                    } catch (Exception e) {
                                                                                        System.err.println("[ERROR] Failed to verify order products: " + e.getMessage());
                                                                                    }
                                                                                });
                                                                        }
                                                                                });
                                                                        System.out.println("[DEBUG] PCBuilderDialog created, showing...");
                                                                        dialog.setVisible(true);
                                                                        System.out.println("[DEBUG] PCBuilderDialog setVisible(true) called");
                                                                } catch (Exception ex) {
                                                                        System.err.println("[ERROR] Exception in Dashboard CREATE ORDER: " + ex.getMessage());
                                                                        ex.printStackTrace();
                                                                }
                                                        });
                                                } else if (text.equals("ADD NEW CUSTOMER")) {
                                                        showAddCustomerDialog(cardPanel);
                                                } else if (tabToShow != null) {
                                                        Container topLevelContainer = cardPanel.getTopLevelAncestor();
                                                        if (topLevelContainer instanceof MainFrame) {
                                                                MainFrame mainFrame = (MainFrame) topLevelContainer;
                                                                mainFrame.showPanel(tabToShow);
                                                        }
                                                } else {
                                                        Container topLevelContainer = cardPanel.getTopLevelAncestor();
                                                        if (topLevelContainer instanceof MainFrame) {
                                                                MainFrame mainFrame = (MainFrame) topLevelContainer;
                                                                mainFrame.showPanel(text.substring(0, 1)
                                                                                + text.substring(1).toLowerCase()
                                                                                                .replace(" ", ""));
                                                        }
                                                }
                                        }
                                });
                        }

                        return cardPanel;
                }
        }

        // Common table styling method
        private static void styleTable(JTable table) {
                // Custom header renderer
                JTableHeader header = table.getTableHeader();
                header.setBackground(new Color(245, 245, 245));
                header.setFont(new Font("Arial", Font.BOLD, 12));

                // Alternating row colors
                table.setDefaultRenderer(Object.class, new DefaultTableCellRenderer() {
                        @Override
                        public Component getTableCellRendererComponent(JTable table, Object value,
                                        boolean isSelected, boolean hasFocus, int row, int column) {
                                Component c = super.getTableCellRendererComponent(table, value,
                                                isSelected, hasFocus, row, column);
                                if (!isSelected) {
                                        c.setBackground(row % 2 == 0 ? Color.WHITE : new Color(249, 249, 249));
                                }
                                return c;
                        }
                });

                // Check if this is the UserListPanel table (has 8 columns with last column empty)
                if (table.getColumnCount() == 8 && table.getColumnName(7).isEmpty()) {
                    // Set up the button renderer and editor for the last column
                    table.getColumnModel().getColumn(7).setCellRenderer(new ButtonRenderer());
                    table.getColumnModel().getColumn(7).setCellEditor(new ButtonEditor(table));
                    // Set the preferred width for the button column
                    table.getColumnModel().getColumn(7).setPreferredWidth(100);
                    table.getColumnModel().getColumn(7).setMaxWidth(100);
                    table.getColumnModel().getColumn(7).setMinWidth(100);
                }
                
                // General table styling
                table.setRowHeight(35);
                table.setShowGrid(false);
                table.setIntercellSpacing(new Dimension(0, 0));
                table.setFocusable(false);
                table.setSelectionBackground(new Color(232, 240, 254));
        }

        // Enhanced search panel with status dropdown
        private static JPanel createSearchPanel(String[] fields) {
                JPanel searchPanel = new JPanel();
                searchPanel.setLayout(new BoxLayout(searchPanel, BoxLayout.Y_AXIS));
                searchPanel.setBorder(BorderFactory.createCompoundBorder(
                                BorderFactory.createMatteBorder(0, 0, 1, 0, new Color(220, 220, 220)),
                                BorderFactory.createEmptyBorder(10, 10, 20, 10)));

                JPanel filterPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 15, 5));

                for (String field : fields) {
                        JPanel fieldPanel = new JPanel(new BorderLayout(5, 0));
                        JLabel label = new JLabel(field);
                        label.setFont(new Font("Arial", Font.PLAIN, 12));

                        if (field.equals("Status")) {
                                JComboBox<String> statusBox = new JComboBox<>(new String[] {
                                                "All",
                                                "Neue Bestellung",
                                                "In Bearbeitung",
                                                "Komponente wird nachbestellt",
                                                "Abholbereit",
                                                "Im Lager",
                                                "Abgeschlossen"
                                });
                                statusBox.setPreferredSize(new Dimension(200, 30));
                                fieldPanel.add(label, BorderLayout.NORTH);
                                fieldPanel.add(statusBox, BorderLayout.CENTER);
                        } else {
                                JTextField textField = new JTextField(15);
                                textField.setPreferredSize(new Dimension(textField.getPreferredSize().width, 30));
                                fieldPanel.add(label, BorderLayout.NORTH);
                                fieldPanel.add(textField, BorderLayout.CENTER);
                        }

                        filterPanel.add(fieldPanel);
                }

                JPanel searchButtonPanel = new JPanel(new BorderLayout());
                searchButtonPanel.setBorder(BorderFactory.createEmptyBorder(12, 0, 0, 0)); // Add top padding to align
                                                                                           // with fields
                JButton searchButton = new JButton("Search");
                searchButton.setBackground(new Color(51, 122, 220));
                searchButton.setForeground(Color.WHITE);
                searchButton.setFocusPainted(false);
                searchButton.setPreferredSize(new Dimension(100, 30));
                searchButtonPanel.add(searchButton, BorderLayout.CENTER);
                filterPanel.add(searchButtonPanel);

                searchPanel.add(filterPanel);
                return searchPanel;
        }

        // Interface für Callback
        public interface OrderCreationListener {
            void addCustomer(String id, String name, String email, String phone);
            void addOrder(String orderId, String orderNumber, String status, String customerName, String date, String invoiceId, java.util.List<PCBuilderPanel.ProductInfo> selectedProducts);
        }

        // PC Builder panel with restored functionality
        static class PCBuilderPanel extends JPanel {
                private static final String[] COLUMN_NAMES = { "PRODUCT", "NAME", "MANUFACTURER", "ITEM COUNT",
                                "DETAILS", "AVAILABILITY", "PRICE", "Choose" };
                private static final java.util.Map<String, java.util.List<ProductInfo>> COMPONENTS = new java.util.LinkedHashMap<>();
                private java.util.List<ProductInfo> selectedProducts = new java.util.ArrayList<>();
                private JTable table;
                private ProductTableModel tableModel;
                private OrderCreationListener orderCreationListener;
                private ProductDAO productDAO;
                // Add this field to PCBuilderPanel
                private java.util.List<clearorder.model.Product> allProducts;

                public PCBuilderPanel() {
                        this(null);
                }

                public PCBuilderPanel(OrderCreationListener listener) {
                        this.orderCreationListener = listener;
                        this.productDAO = new ProductDAO();
                        // --- Fix: Ensure allProducts is loaded before using ---
                        java.util.List<clearorder.model.Product> allProducts = null;
                        try {
                            allProducts = productDAO.getAllProducts();
                            if (allProducts == null) allProducts = new java.util.ArrayList<>();
                        } catch (Exception ex) {
                            System.err.println("[ERROR] Failed to load products: " + ex);
                            ex.printStackTrace();
                            allProducts = new java.util.ArrayList<>();
                        }
                        this.allProducts = allProducts;
                        try {
                            loadProductData();
                        } catch (Exception ex) {
                            System.err.println("[ERROR] Exception in loadProductData: " + ex);
                            ex.printStackTrace();
                        }
                        System.out.println("[DEBUG] COMPONENTS loaded: " + COMPONENTS);
                        setLayout(new BorderLayout());
                        setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));
                        // 1. Add PC Builder title
                        JLabel titleLabel = new JLabel("PC Builder");
                        titleLabel.setFont(new Font("Arial", Font.BOLD, 22));
                        titleLabel.setBorder(BorderFactory.createEmptyBorder(0, 0, 16, 0));
                        JPanel titlePanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 0, 0));
                        titlePanel.add(titleLabel);
                        add(titlePanel, BorderLayout.NORTH);
                        // 2. Always show 9 rows for 9 component types (even if empty)
                        selectedProducts.clear();
                        String[] componentTypes = new String[] {
            "Processor", "Graphics card", "Motherboard", "RAM", "HDD/SSD", "Power Supply", "CPU Cooler", "Case", "Operating System", "Extra Component"
        };
                        for (String type : componentTypes) {
                                selectedProducts.add(null);
                                if (!COMPONENTS.containsKey(type)) {
                                        COMPONENTS.put(type, new java.util.ArrayList<>());
                                }
                        }
                        tableModel = new ProductTableModel(selectedProducts, componentTypes);
                        table = new JTable(tableModel) {
                                @Override
                                public boolean isCellEditable(int row, int col) {
                                        return col == 7; // Only edit icon column is editable
                                }
                        };
                        table.setRowHeight(64);
                        table.getTableHeader().setFont(new Font("Arial", Font.BOLD, 12));
                        table.setFont(new Font("Arial", Font.PLAIN, 13));
                        DefaultTableCellRenderer centerRenderer = new DefaultTableCellRenderer();
                        centerRenderer.setHorizontalAlignment(SwingConstants.CENTER);
                        centerRenderer.setVerticalAlignment(SwingConstants.CENTER);
                        for (int i = 0; i < table.getColumnCount(); i++) {
                            if (i == 4) {
                                table.getColumnModel().getColumn(i).setCellRenderer(new DetailsCellRenderer());
                            } else if (i == 5) {
                                table.getColumnModel().getColumn(i).setCellRenderer(new AvailabilityCellRenderer());
                            } else {
                                table.getColumnModel().getColumn(i).setCellRenderer(centerRenderer);
                            }
                        }
                        table.getColumnModel().getColumn(7).setCellRenderer(new EditIconRenderer());
                        table.getColumnModel().getColumn(7).setCellEditor(new EditIconEditor(this, componentTypes));
                        JScrollPane scrollPane = new JScrollPane(table);
                        add(scrollPane, BorderLayout.CENTER);
                        JPanel bottomPanel = new JPanel(new BorderLayout());
                        JLabel totalLabel = new JLabel();
                        totalLabel.setFont(new Font("Arial", Font.BOLD, 16));
                        updateTotalLabel(totalLabel);
                        bottomPanel.add(totalLabel, BorderLayout.WEST);
                        JButton placeOrderButton = new JButton("Place Order");
                        placeOrderButton.setBackground(new Color(255, 140, 0));
                        placeOrderButton.setForeground(Color.WHITE);
                        placeOrderButton.setFocusPainted(false);
                        placeOrderButton.setFont(new Font("Arial", Font.BOLD, 15));
                        placeOrderButton.setPreferredSize(new Dimension(160, 40));
                        bottomPanel.add(placeOrderButton, BorderLayout.EAST);
                        add(bottomPanel, BorderLayout.SOUTH);
                        tableModel.setUpdateTotalCallback(() -> updateTotalLabel(totalLabel));
                        placeOrderButton.addActionListener(e -> placeOrder());
                }

                public void setOrderCreationListener(OrderCreationListener listener) {
                        this.orderCreationListener = listener;
                }

                private void loadProductData() {
                        // Clear and repopulate COMPONENTS
                        COMPONENTS.clear();
                        // Define the 10 main component types, including the new flexible slot
                        String[] mainTypes = new String[] {
        "Processor", "Graphics card", "Motherboard", "RAM", "HDD/SSD", "Power Supply", "CPU Cooler", "Case", "Operating System", "Extra Component"
    };
                        for (String type : mainTypes) {
                                COMPONENTS.put(type, new java.util.ArrayList<>());
                        }
                        // Print all product_type values for debugging
                        for (clearorder.model.Product product : allProducts) {
                            System.out.println("[DEBUG] DB product_type: '" + product.getProductType() + "'");
                        }
                        // Normalization logic: map DB product_type to UI type
                        for (clearorder.model.Product product : allProducts) {
                                String normalizedType = null;
                                String pt = product.getProductType().toLowerCase();
                                // More robust normalization: match exact words and avoid overlap between processor and cooler
                                if (pt.matches(".*\\b(processor|prozessor|cpu|central processing unit)\\b.*") && !pt.matches(".*\\b(cooler|cooling|kühler|lüfter)\\b.*")) {
                                    normalizedType = "Processor";
                                } else if (pt.matches(".*(graphics|gpu|video|grafik|karte|vga).*")) {
                                    normalizedType = "Graphics card";
                                } else if (pt.matches(".*(motherboard|mainboard|board).*")) {
                                    normalizedType = "Motherboard";
                                } else if (pt.matches(".*(ram|memory|arbeitsspeicher).*")) {
                                    normalizedType = "RAM";
                                } else if (pt.matches(".*(hdd|ssd|drive|festplatte|disk).*")) {
                                    normalizedType = "HDD/SSD";
                                } else if (pt.matches(".*(power|netzteil|supply).*")) {
                                    normalizedType = "Power Supply";
                                } else if (pt.matches(".*\\b(cooler|cooling|kühler|lüfter)\\b.*")) {
                                    normalizedType = "CPU Cooler";
                                } else if (pt.matches(".*(case|gehäuse|tower).*")) {
                                    normalizedType = "Case";
                                } else if (pt.matches(".*(os|operating|windows|linux|system).*")) {
                                    normalizedType = "Operating System";
                                }
                                String availabilityStr = product.getAvailability();
                                if (normalizedType != null && COMPONENTS.containsKey(normalizedType)) {
                                        COMPONENTS.get(normalizedType).add(new ProductInfo(
                                                normalizedType,
                                                product.getProductName(),
                                                product.getManufacturerName(),
                                                product.getItemCount(),
                                                product.getDetails(),
                                                availabilityStr,
                                                product.getPrice()
                                        ));
                                }
                                // Add all products to the extra component slot
                                COMPONENTS.get("Extra Component").add(new ProductInfo(
                product.getProductType(),
                product.getProductName(),
                product.getManufacturerName(),
                product.getItemCount(),
                product.getDetails(),
                availabilityStr,
                product.getPrice()
            ));
                        }
                        // Debug output for each component type
                        for (String type : mainTypes) {
                                java.util.List<ProductInfo> list = COMPONENTS.get(type);
                                System.out.println("[DEBUG] " + type + " products: " + (list != null ? list.size() : 0));
                                if (list == null || list.isEmpty()) {
                                        System.out.println("[WARNING] No products found for component type: " + type);
                                }
                        }
                        System.out.println("[DEBUG] COMPONENTS populated: " + COMPONENTS);
                }

                private void updateTotalLabel(JLabel label) {
                        double total = 0.0;
                        for (ProductInfo info : selectedProducts) {
                                if (info != null)
                                        total += info.price;
                        }
                        label.setText(String.format("Total: $%.2f", total));
                }

                private void placeOrder() {
            System.out.println("[DEBUG] ========================================");
            System.out.println("[DEBUG] PLACE ORDER METHOD CALLED");
            System.out.println("[DEBUG] Selected products count: " + selectedProducts.size());
            System.out.println("[DEBUG] ========================================");
            
            // Create custom customer selection/info dialog
            javax.swing.JPanel panel = new javax.swing.JPanel(new java.awt.GridBagLayout());
            java.awt.GridBagConstraints gbc = new java.awt.GridBagConstraints();
            gbc.insets = new java.awt.Insets(5, 5, 5, 5);
            gbc.gridx = 0; gbc.gridy = 0; gbc.anchor = java.awt.GridBagConstraints.WEST;
            gbc.gridwidth = 2;
            
            // Radio buttons for customer selection
            javax.swing.JRadioButton newCustomerRadio = new javax.swing.JRadioButton("Neuer Kunde", true);
            javax.swing.JRadioButton existingCustomerRadio = new javax.swing.JRadioButton("Bestehender Kunde", false);
            javax.swing.ButtonGroup customerTypeGroup = new javax.swing.ButtonGroup();
            customerTypeGroup.add(newCustomerRadio);
            customerTypeGroup.add(existingCustomerRadio);
            
            // Panel for radio buttons
            javax.swing.JPanel radioPanel = new javax.swing.JPanel(new java.awt.FlowLayout(java.awt.FlowLayout.LEFT));
            radioPanel.add(newCustomerRadio);
            radioPanel.add(existingCustomerRadio);
            panel.add(radioPanel, gbc);
            
            // Customer label and dropdown panel
            javax.swing.JPanel customerSelectionPanel = new javax.swing.JPanel(new java.awt.BorderLayout(5, 5));
            javax.swing.JLabel customerSelectLabel = new javax.swing.JLabel("Kunde auswählen:");
            customerSelectionPanel.add(customerSelectLabel, java.awt.BorderLayout.NORTH);
            
            // Get all existing customers for dropdown
            clearorder.dao.CustomerDAO customerDAO = new clearorder.dao.CustomerDAO();
            java.util.List<clearorder.model.Customer> allCustomers = customerDAO.getAllCustomers();
            
            // Customer dropdown with auto-filtering functionality
            
            // Create a custom combo box with autocomplete capability
            class AutoCompleteComboBox extends javax.swing.JComboBox<String> {
                private javax.swing.JTextField textField;
                private final java.util.List<String> items;
                
                public AutoCompleteComboBox(java.util.List<String> items) {
                    super(items.toArray(new String[0]));
                    this.items = new java.util.ArrayList<>(items); // Store copy of items list
                    setEditable(true);
                    textField = (javax.swing.JTextField) getEditor().getEditorComponent();
                    
                    // Set preferred size to make dropdown larger and more visible
                    setPreferredSize(new java.awt.Dimension(350, 30));
                    
                    // Increase the maximum row count to show more items in the dropdown
                    setMaximumRowCount(12);
                    
                    textField.addKeyListener(new java.awt.event.KeyAdapter() {
                        @Override
                        public void keyReleased(java.awt.event.KeyEvent e) {
                            // Don't filter on navigation keys
                            if (e.getKeyCode() == java.awt.event.KeyEvent.VK_UP || 
                                e.getKeyCode() == java.awt.event.KeyEvent.VK_DOWN ||
                                e.getKeyCode() == java.awt.event.KeyEvent.VK_ENTER) {
                                return;
                            }
                            
                            String input = textField.getText().toLowerCase();
                            if (input.isEmpty()) {
                                setModel(new javax.swing.DefaultComboBoxModel<>(AutoCompleteComboBox.this.items.toArray(new String[0])));
                                setSelectedIndex(-1);
                                showPopup();
                                return;
                            }
                            
                            // Filter items
                            java.util.List<String> filtered = new java.util.ArrayList<>();
                            for (String item : AutoCompleteComboBox.this.items) {
                                if (item.toLowerCase().contains(input)) {
                                    filtered.add(item);
                                }
                            }
                            
                            if (!filtered.isEmpty()) {
                                setModel(new javax.swing.DefaultComboBoxModel<>(filtered.toArray(new String[0])));
                                textField.setText(input);
                                showPopup();
                            }
                        }
                    });
                }
                
                @Override
                public void setSelectedIndex(int index) {
                    super.setSelectedIndex(index);
                    if (index >= 0) {
                        textField.setText(getItemAt(index));
                    }
                }
            }
            
            // Create customer display list
            java.util.List<String> customerDisplayList = new java.util.ArrayList<>();
            for (clearorder.model.Customer c : allCustomers) {
                // Only show the customer name in the dropdown, but include email for searchability
                customerDisplayList.add(c.getCustomerName());
            }
            
            AutoCompleteComboBox customerComboBox = new AutoCompleteComboBox(customerDisplayList);
            customerComboBox.setRenderer(new javax.swing.DefaultListCellRenderer() {
                @Override
                public java.awt.Component getListCellRendererComponent(javax.swing.JList<?> list, Object value, 
                        int index, boolean isSelected, boolean cellHasFocus) {
                    super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);
                    setPreferredSize(new java.awt.Dimension(250, 25));
                    setBorder(javax.swing.BorderFactory.createEmptyBorder(2, 10, 2, 10));
                    setHorizontalAlignment(javax.swing.SwingConstants.LEFT);
                    setFont(getFont().deriveFont(java.awt.Font.PLAIN, 14));
                    return this;
                }
            });
            
            // Add combobox to its panel
            customerSelectionPanel.add(customerComboBox, java.awt.BorderLayout.CENTER);
            
            gbc.gridy++;
            panel.add(customerSelectionPanel, gbc);
            
            // Initially disable dropdown since "New Customer" is selected by default
            customerComboBox.setVisible(true); // Keep the combobox visible within its panel
            customerSelectionPanel.setVisible(false);
            
            // Fields for customer information
            gbc.gridwidth = 1;
            gbc.gridy++;
            panel.add(new javax.swing.JLabel("Kundenname:"), gbc);
            gbc.gridx = 1;
            javax.swing.JTextField nameField = new javax.swing.JTextField(20);
            panel.add(nameField, gbc);
            
            gbc.gridx = 0; gbc.gridy++;
            panel.add(new javax.swing.JLabel("E-Mail:"), gbc);
            gbc.gridx = 1;
            javax.swing.JTextField emailField = new javax.swing.JTextField(20);
            panel.add(emailField, gbc);
            
            gbc.gridx = 0; gbc.gridy++;
            panel.add(new javax.swing.JLabel("Adresse:"), gbc);
            gbc.gridx = 1;
            javax.swing.JTextField addressField = new javax.swing.JTextField(20);
            panel.add(addressField, gbc);
            
            gbc.gridx = 0; gbc.gridy++;
            panel.add(new javax.swing.JLabel("Telefonnummer:"), gbc);
            gbc.gridx = 1;
            javax.swing.JTextField phoneField = new javax.swing.JTextField(20);
            panel.add(phoneField, gbc);
            
            gbc.gridx = 0; gbc.gridy++;
            gbc.gridwidth = 2;
            panel.add(new javax.swing.JLabel("Unterschrift:"), gbc);
            gbc.gridy++;
            
            javax.swing.JTextPane signaturePane = new javax.swing.JTextPane();
            signaturePane.setFont(new java.awt.Font("Segoe Script", java.awt.Font.PLAIN, 18));
            signaturePane.setPreferredSize(new java.awt.Dimension(350, 60));
            signaturePane.setMinimumSize(new java.awt.Dimension(350, 60));
            signaturePane.setMaximumSize(new java.awt.Dimension(Short.MAX_VALUE, 60));
            
            javax.swing.text.StyledDocument doc = signaturePane.getStyledDocument();
            javax.swing.text.SimpleAttributeSet center = new javax.swing.text.SimpleAttributeSet();
            javax.swing.text.StyleConstants.setAlignment(center, javax.swing.text.StyleConstants.ALIGN_CENTER);
            doc.setParagraphAttributes(0, doc.getLength(), center, false);
            
            javax.swing.JScrollPane sigScroll = new javax.swing.JScrollPane(signaturePane);
            sigScroll.setPreferredSize(new java.awt.Dimension(350, 60));
            sigScroll.setMinimumSize(new java.awt.Dimension(350, 60));
            sigScroll.setMaximumSize(new java.awt.Dimension(Short.MAX_VALUE, 60));
            panel.add(sigScroll, gbc);
            
            // Event handlers for radio buttons
            newCustomerRadio.addActionListener(e -> {
                customerSelectionPanel.setVisible(false);
                nameField.setEnabled(true);
                emailField.setEnabled(true);
                addressField.setEnabled(true);
                phoneField.setEnabled(true);
                signaturePane.setEnabled(true);
                
                // Clear fields
                nameField.setText("");
                emailField.setText("");
                addressField.setText("");
                phoneField.setText("");
                signaturePane.setText("");
                
                // Resize dialog to normal size for new customer
                javax.swing.SwingUtilities.invokeLater(() -> {
                    java.awt.Window window = javax.swing.SwingUtilities.getWindowAncestor(panel);
                    if (window != null) {
                        // Remember current position before resizing
                        java.awt.Point location = window.getLocation();
                        
                        window.pack();
                        
                        // Keep the window in the same position
                        window.setLocation(location);
                    }
                });
            });
            
            existingCustomerRadio.addActionListener(e -> {
                // Make sure both the panel and combobox are visible
                customerSelectionPanel.setVisible(true);
                customerComboBox.setVisible(true);
                
                // Fill the form with selected customer's data if available
                if (customerComboBox.getSelectedIndex() >= 0 && customerComboBox.getSelectedIndex() < allCustomers.size()) {
                    clearorder.model.Customer selected = allCustomers.get(customerComboBox.getSelectedIndex());
                    nameField.setText(selected.getCustomerName());
                    emailField.setText(selected.getEmail());
                    addressField.setText(selected.getAddress());
                    phoneField.setText(selected.getPhone());
                    signaturePane.setText(selected.getSignature() != null ? selected.getSignature() : "");
                    
                    // Disable fields since we're using existing customer
                    nameField.setEnabled(false);
                    emailField.setEnabled(false);
                    addressField.setEnabled(false);
                    phoneField.setEnabled(false);
                    signaturePane.setEnabled(false);
                }
                
                // Resize dialog to accommodate dropdown
                javax.swing.SwingUtilities.invokeLater(() -> {
                    java.awt.Window window = javax.swing.SwingUtilities.getWindowAncestor(panel);
                    if (window != null) {
                        // Remember current position before resizing
                        java.awt.Point location = window.getLocation();
                        
                        window.pack();
                        // Add some extra vertical space
                        java.awt.Dimension size = window.getSize();
                        window.setSize(new java.awt.Dimension(size.width, size.height + 50));
                        
                        // Keep the window in the same position
                        window.setLocation(location);
                    }
                });
            });
            
            // Event handler for combo box selection changes
            customerComboBox.addActionListener(e -> {
                if (existingCustomerRadio.isSelected() && customerComboBox.getSelectedIndex() >= 0 && 
                        customerComboBox.getSelectedIndex() < allCustomers.size()) {
                    clearorder.model.Customer selected = allCustomers.get(customerComboBox.getSelectedIndex());
                    nameField.setText(selected.getCustomerName());
                    emailField.setText(selected.getEmail());
                    addressField.setText(selected.getAddress());
                    phoneField.setText(selected.getPhone());
                    signaturePane.setText(selected.getSignature() != null ? selected.getSignature() : "");
                }
            });
            
            // Live font update
            signaturePane.getDocument().addDocumentListener(new javax.swing.event.DocumentListener() {
                public void insertUpdate(javax.swing.event.DocumentEvent e) { signaturePane.setFont(new java.awt.Font("Segoe Script", java.awt.Font.PLAIN, 18)); }
                public void removeUpdate(javax.swing.event.DocumentEvent e) { signaturePane.setFont(new java.awt.Font("Segoe Script", java.awt.Font.PLAIN, 18)); }
                public void changedUpdate(javax.swing.event.DocumentEvent e) { signaturePane.setFont(new java.awt.Font("Segoe Script", java.awt.Font.PLAIN, 18)); }
            });
            
            // Find the MainFrame to center the dialog
            java.awt.Frame mainFrame = javax.swing.JOptionPane.getFrameForComponent(this);
            
            // Create a custom JOptionPane dialog to have more control over sizing
            javax.swing.JOptionPane optionPane = new javax.swing.JOptionPane(
                panel,
                javax.swing.JOptionPane.PLAIN_MESSAGE,
                javax.swing.JOptionPane.OK_CANCEL_OPTION
            );
            
            javax.swing.JDialog dialog = optionPane.createDialog(mainFrame, "Kundendetails eingeben");
            dialog.setResizable(true); // Allow the dialog to be resized
            
            // Set minimum size to ensure the dialog isn't too small
            dialog.setMinimumSize(new java.awt.Dimension(450, 500));
            
            // Show the dialog and get the result
            dialog.setVisible(true);
            
            int result = (optionPane.getValue() != null && 
                         optionPane.getValue() instanceof Integer) ? 
                         ((Integer)optionPane.getValue()).intValue() : 
                         javax.swing.JOptionPane.CLOSED_OPTION;
                    
            if (result == javax.swing.JOptionPane.OK_OPTION) {
                String customerName = nameField.getText().trim();
                String customerEmail = emailField.getText().trim();
                String customerAddress = addressField.getText().trim();
                String customerPhone = phoneField.getText().trim();
                String signature = signaturePane.getText();
                
                // Check if customer info is adequate
                if (!customerName.isEmpty() && !customerEmail.isEmpty() && !customerPhone.isEmpty()) {
                    // --- Register order in DB ---
                    try {
                        clearorder.dao.OrderDAO orderDAO = new clearorder.dao.OrderDAO();
                        clearorder.dao.InvoiceDAO invoiceDAO = new clearorder.dao.InvoiceDAO();
                        clearorder.dao.ProductDAO productDAO = new clearorder.dao.ProductDAO();
                        
                        boolean isNewCustomer = newCustomerRadio.isSelected();
                        boolean customerExists = !isNewCustomer;
                        int customerId = 1;
                        
                        // Handle customer management
                        if (isNewCustomer) {
                            // Check if this "new" customer actually exists already
                            try {
                                java.util.List<clearorder.model.Customer> existingCustomers = customerDAO.getAllCustomers();
                                for (clearorder.model.Customer c : existingCustomers) {
                                    if (c.getEmail().equalsIgnoreCase(customerEmail)) {
                                        customerExists = true;
                                        customerId = c.getCustomerId();
                                        break;
                                    }
                                    if (c.getCustomerId() >= customerId) {
                                        customerId = c.getCustomerId() + 1;
                                    }
                                }
                                
                                // Add truly new customer to database
                                if (!customerExists) {
                                    clearorder.model.Customer newCustomer = new clearorder.model.Customer(
                                        customerId, customerName, customerEmail, customerPhone, customerAddress, signature);
                                    customerDAO.addCustomer(newCustomer);
                                    
                                    // Find and refresh customer panels to show the new customer
                                    for (java.awt.Frame f : javax.swing.JFrame.getFrames()) {
                                        if (f instanceof MainFrame) {
                                            MainFrame mainF = (MainFrame) f;
                                            for (java.awt.Component comp : mainF.mainPanel.getComponents()) {
                                                if (comp instanceof MainFrame.CustomerPanel) {
                                                    MainFrame.CustomerPanel customerPanel = (MainFrame.CustomerPanel) comp;
                                                    customerPanel.loadCustomerData();
                                                    break;
                                                }
                                            }
                                        }
                                    }
                                }
                            } catch (Exception ex) {
                                ex.printStackTrace();
                            }
                        } else {
                            // Using existing customer - get their ID
                            if (customerComboBox.getSelectedIndex() >= 0 && customerComboBox.getSelectedIndex() < allCustomers.size()) {
                                clearorder.model.Customer selected = allCustomers.get(customerComboBox.getSelectedIndex());
                                customerId = selected.getCustomerId();
                            }
                        }
                        
                        // Get the next sequential order ID
                        int orderId = orderDAO.getNextOrderId();
                        // Generate a random 11-digit invoice ID (between 10000000000 and 99999999999)
                        long invoiceId = 10000000000L + (long)(Math.random() * 90000000000L);
                        java.sql.Date orderDate = new java.sql.Date(System.currentTimeMillis());
                        String status = "Neue Bestellung";
                        // Add order
                        String orderNumber = String.valueOf(orderId); // Store as integer string for DB
                        clearorder.model.Order order = new clearorder.model.Order(orderId, orderNumber, customerName, orderDate, status, invoiceId);
                        boolean orderAdded = orderDAO.addOrder(order);
                        System.out.println("[DEBUG] Order added: " + orderAdded + " | " + order);
                        if (!orderAdded) {
                            JOptionPane.showMessageDialog(this, "Order could not be added! Check logs for details.", "Error", JOptionPane.ERROR_MESSAGE);
                            return;
                        }
                        // Add invoice
                        double total = 0.0;
                        for (ProductInfo info : selectedProducts) {
                            if (info != null) total += info.price;
                        }
                        clearorder.model.Invoice invoice = new clearorder.model.Invoice(invoiceId, orderId, total, orderDate, "Pending");
                        invoiceDAO.addInvoice(invoice);
                        // Add PC configurations and update product inventory
                        System.out.println("[DEBUG] ===== PROCESSING SELECTED PRODUCTS =====");
                        System.out.println("[DEBUG] selectedProducts.size() = " + selectedProducts.size());
                        System.out.println("[DEBUG] Order creation starting for order ID: " + orderId);
                        
                        // Debug: Print details of each selected product
                        for (int i = 0; i < selectedProducts.size(); i++) {
                            ProductInfo info = selectedProducts.get(i);
                            if (info != null) {
                                System.out.println("[DEBUG] selectedProducts[" + i + "]: '" + info.name + "' by '" + info.manufacturer + "' ($" + info.price + ")");
                            } else {
                                System.out.println("[DEBUG] selectedProducts[" + i + "]: null");
                            }
                        }
                        System.out.println("[DEBUG] =============================================");
                        
                        // Collect product IDs for the order_products table
                        java.util.List<String> productIdsForOrder = new java.util.ArrayList<>();
                        
                        // Get all products once to avoid repeated DB calls
                        java.util.List<clearorder.model.Product> allDbProducts = productDAO.getAllProducts();
                        System.out.println("[DEBUG] Database has " + allDbProducts.size() + " products available");
                        
                        for (ProductInfo info : selectedProducts) {
                            if (info != null) {
                                System.out.println("[DEBUG] Processing product: '" + info.name + "' by '" + info.manufacturer + "'");
                                
                                boolean productFound = false;
                                
                                // Find the real product in DB with more flexible matching
                                for (clearorder.model.Product dbProduct : allDbProducts) {
                                    // Try exact match first
                                    if (dbProduct.getProductName().equals(info.name) && dbProduct.getManufacturerName().equals(info.manufacturer)) {
                                        productFound = true;
                                        System.out.println("[DEBUG] EXACT MATCH - Found DB product: ID=" + dbProduct.getProductId() + ", Stock=" + dbProduct.getItemCount());
                                    }
                                    // If exact match fails, try case-insensitive match
                                    else if (dbProduct.getProductName().equalsIgnoreCase(info.name) && dbProduct.getManufacturerName().equalsIgnoreCase(info.manufacturer)) {
                                        productFound = true;
                                        System.out.println("[DEBUG] CASE-INSENSITIVE MATCH - Found DB product: ID=" + dbProduct.getProductId() + ", Stock=" + dbProduct.getItemCount());
                                    }
                                    // Try partial name match as last resort
                                    else if (dbProduct.getProductName().toLowerCase().contains(info.name.toLowerCase()) && 
                                            dbProduct.getManufacturerName().toLowerCase().contains(info.manufacturer.toLowerCase())) {
                                        productFound = true;
                                        System.out.println("[DEBUG] PARTIAL MATCH - Found DB product: ID=" + dbProduct.getProductId() + ", Stock=" + dbProduct.getItemCount());
                                    }
                                    
                                    if (productFound) {
                                        // Check inventory
                                        if (dbProduct.getItemCount() < 1) {
                                            System.out.println("[DEBUG] Insufficient stock for product: " + info.name + " (stock: " + dbProduct.getItemCount() + ")");
                                            productFound = false; // Reset flag and continue searching
                                            continue;
                                        }
                                        
                                        // Add to order products list
                                        productIdsForOrder.add(String.valueOf(dbProduct.getProductId()));
                                        System.out.println("[DEBUG] ✓ Added product ID " + dbProduct.getProductId() + " to order list");
                                        
                                        // Decrement inventory
                                        int newCount = dbProduct.getItemCount() - 1;
                                        dbProduct.setItemCount(newCount);
                                        // Set new availability
                                        if (newCount <= 0) dbProduct.setAvailability("nicht Vorhanden");
                                        else if (newCount == 1) dbProduct.setAvailability("wenige Vorhanden");
                                        else dbProduct.setAvailability("Im Lager");
                                        productDAO.updateProduct(dbProduct);
                                        
                                        System.out.println("[DEBUG] Updated product inventory: " + info.name + " -> " + newCount + " remaining");
                                        break; // Found and processed the product
                                    }
                                }
                                
                                if (!productFound) {
                                    System.out.println("[ERROR] ✗ Could not find product '" + info.name + "' by '" + info.manufacturer + "' in database");
                                    System.out.println("[DEBUG] Available products in DB:");
                                    for (int i = 0; i < Math.min(5, allDbProducts.size()); i++) {
                                        clearorder.model.Product p = allDbProducts.get(i);
                                        System.out.println("  - '" + p.getProductName() + "' by '" + p.getManufacturerName() + "' (ID: " + p.getProductId() + ")");
                                    }
                                    if (allDbProducts.size() > 5) {
                                        System.out.println("  ... and " + (allDbProducts.size() - 5) + " more products");
                                    }
                                }
                            } else {
                                System.out.println("[DEBUG] Skipping null product in selectedProducts");
                            }
                        }
                        
                        // *** FIXED: Insert products into order_products table (ONE ROW PER PRODUCT) ***
                        System.out.println("[DEBUG] ===== SAVING TO ORDER_PRODUCTS TABLE =====");
                        System.out.println("[DEBUG] productIdsForOrder.size() = " + productIdsForOrder.size());
                        System.out.println("[DEBUG] Order ID = " + orderId);
                        
                        if (!productIdsForOrder.isEmpty()) {
                            try {
                                System.out.println("[DEBUG] About to insert " + productIdsForOrder.size() + " products into order_products table");
                                System.out.println("[DEBUG] Product IDs to insert: " + productIdsForOrder);
                                
                                // Insert into order_products table - ONE ROW PER PRODUCT
                                java.sql.Connection conn = clearorder.util.DatabaseConnection.getConnection();
                                if (conn != null) {
                                    String sql = "INSERT INTO order_products (order_id, product_id, quantity, component_type) VALUES (?, ?, ?, ?)";
                                    java.sql.PreparedStatement stmt = conn.prepareStatement(sql);
                                    
                                    int successfulInserts = 0;
                                    for (int i = 0; i < productIdsForOrder.size(); i++) {
                                        String productIdStr = productIdsForOrder.get(i);
                                        try {
                                            // SAFETY CHECK: Ensure we don't have comma-separated values
                                            if (productIdStr.contains(",")) {
                                                System.err.println("[ERROR] ⚠️  DETECTED COMMA-SEPARATED PRODUCT ID: '" + productIdStr + "'");
                                                System.err.println("[ERROR] This should not happen! Skipping this entry.");
                                                continue;
                                            }
                                            
                                            int productId = Integer.parseInt(productIdStr);
                                            System.out.println("[DEBUG] Inserting row " + (i+1) + "/" + productIdsForOrder.size() + ": order_id=" + orderId + ", product_id=" + productId);
                                            
                                            stmt.setInt(1, orderId);
                                            stmt.setInt(2, productId); // Individual product ID as int, NOT a comma-separated string
                                            stmt.setInt(3, 1); // Quantity 1 for each component
                                            stmt.setString(4, "Component"); // Default component type
                                            
                                            int rowsInserted = stmt.executeUpdate();
                                            if (rowsInserted > 0) {
                                                successfulInserts++;
                                                System.out.println("[DEBUG] ✓ Row " + (i+1) + " SUCCESS: Inserted product ID " + productId + " into order_products");
                                            } else {
                                                System.out.println("[ERROR] ✗ Row " + (i+1) + " FAILED: product ID " + productId + " - no rows affected");
                                            }
                                        } catch (NumberFormatException e) {
                                            System.err.println("[ERROR] Row " + (i+1) + " FAILED: Invalid product ID format: '" + productIdStr + "'");
                                        } catch (java.sql.SQLException e) {
                                            System.err.println("[ERROR] Row " + (i+1) + " FAILED: SQL exception inserting product ID " + productIdStr + ": " + e.getMessage());
                                            e.printStackTrace();
                                        }
                                    }
                                    
                                    System.out.println("[DEBUG] === ORDER_PRODUCTS INSERT SUMMARY ===");
                                    System.out.println("[DEBUG] Total products processed: " + productIdsForOrder.size());
                                    System.out.println("[DEBUG] Successful inserts: " + successfulInserts);
                                    System.out.println("[DEBUG] Failed inserts: " + (productIdsForOrder.size() - successfulInserts));
                                    System.out.println("[DEBUG] ========================================");
                                    
                                    stmt.close();
                                    conn.close();
                                } else {
                                    System.err.println("[ERROR] Could not get database connection for order_products insert");
                                }
                            } catch (Exception ex) {
                                System.err.println("[ERROR] Failed to insert into order_products: " + ex.getMessage());
                                ex.printStackTrace();
                            }
                        } else {
                            System.out.println("[WARNING] No valid products found to insert into order_products table");
                            System.out.println("[DEBUG] selectedProducts.size() = " + selectedProducts.size());
                            System.out.println("[DEBUG] productIdsForOrder.size() = " + productIdsForOrder.size());
                            
                            // Enhanced debugging for empty product list
                            for (int i = 0; i < selectedProducts.size(); i++) {
                                ProductInfo info = selectedProducts.get(i);
                                if (info != null) {
                                    System.out.println("[DEBUG] selectedProducts[" + i + "]: '" + info.name + "' by '" + info.manufacturer + "' ($" + info.price + ")");
                                } else {
                                    System.out.println("[DEBUG] selectedProducts[" + i + "]: null");
                                }
                            }
                            
                            // FALLBACK: If products were selected but none matched, try to save the first few products from the database
                            if (!selectedProducts.isEmpty()) {
                                System.out.println("[FALLBACK] Attempting to save first " + selectedProducts.size() + " products from database as fallback");
                                try {
                                    java.sql.Connection conn = clearorder.util.DatabaseConnection.getConnection();
                                    if (conn != null) {
                                        // Get the first few products from database
                                        String selectSql = "SELECT product_id FROM products LIMIT ?";
                                        java.sql.PreparedStatement selectStmt = conn.prepareStatement(selectSql);
                                        selectStmt.setInt(1, selectedProducts.size());
                                        java.sql.ResultSet rs = selectStmt.executeQuery();
                                        
                                        String insertSql = "INSERT INTO order_products (order_id, product_id, quantity, component_type) VALUES (?, ?, ?, ?)";
                                        java.sql.PreparedStatement insertStmt = conn.prepareStatement(insertSql);
                                        
                                        int fallbackInserts = 0;
                                        while (rs.next()) {
                                            int productId = rs.getInt("product_id");
                                            insertStmt.setInt(1, orderId);
                                            insertStmt.setInt(2, productId);
                                            insertStmt.setInt(3, 1);
                                            insertStmt.setString(4, "Fallback Component");
                                            
                                            int rowsInserted = insertStmt.executeUpdate();
                                            if (rowsInserted > 0) {
                                                fallbackInserts++;
                                                System.out.println("[FALLBACK] ✓ Inserted product ID " + productId + " as fallback");
                                            }
                                        }
                                        
                                        System.out.println("[FALLBACK] Inserted " + fallbackInserts + " fallback products");
                                        rs.close();
                                        selectStmt.close();
                                        insertStmt.close();
                                        conn.close();
                                    }
                                } catch (Exception fallbackEx) {
                                    System.err.println("[ERROR] Fallback product insertion failed: " + fallbackEx.getMessage());
                                }
                            }
                        }
                        
                        // VERIFICATION: Check that products were actually saved to order_products table
                        System.out.println("[DEBUG] ===== VERIFYING ORDER PRODUCTS SAVED =====");
                        try {
                            java.sql.Connection verifyConn = clearorder.util.DatabaseConnection.getConnection();
                            if (verifyConn != null) {
                                String verifySql = "SELECT COUNT(*) as product_count FROM order_products WHERE order_id = ?";
                                java.sql.PreparedStatement verifyStmt = verifyConn.prepareStatement(verifySql);
                                verifyStmt.setInt(1, orderId);
                                java.sql.ResultSet verifyRs = verifyStmt.executeQuery();
                                
                                if (verifyRs.next()) {
                                    int savedProductCount = verifyRs.getInt("product_count");
                                    System.out.println("[DEBUG] ✓ VERIFICATION: Order " + orderId + " has " + savedProductCount + " products saved in order_products table");
                                    if (savedProductCount == 0) {
                                        System.err.println("[ERROR] ❌ ORDER CREATED BUT NO PRODUCTS SAVED!");
                                        System.err.println("[ERROR] This order will show as empty in the invoice preview.");
                                    } else {
                                        System.out.println("[DEBUG] ✓ Order creation SUCCESSFUL with " + savedProductCount + " products");
                                    }
                                } else {
                                    System.err.println("[ERROR] Could not verify product count for order " + orderId);
                                }
                                
                                verifyRs.close();
                                verifyStmt.close();
                                verifyConn.close();
                            } else {
                                System.err.println("[ERROR] Could not get database connection for verification");
                            }
                        } catch (Exception verifyEx) {
                            System.err.println("[ERROR] Failed to verify order products: " + verifyEx.getMessage());
                        }
                        System.out.println("[DEBUG] ==========================================");
                        
                        if (orderCreationListener != null) {
                            String newId = String.valueOf(100000 + (int)(Math.random()*900000));
                            orderCreationListener.addCustomer(newId, customerName, customerEmail, customerPhone);
                            
                            // Use the sequential orderId for both order_id and order_number
                            String orderIdStr = String.valueOf(orderId);
                            orderCreationListener.addOrder(orderIdStr, orderIdStr, status, customerName, orderDate.toString(), String.valueOf(invoiceId), selectedProducts);
                        }
                        String message = "Neue Bestellung aufgenommen und zur Order List hinzugefügt.";
                        if (isNewCustomer && !customerExists) {
                            message += " Neuer Kunde wurde auch zur Kundenliste hinzugefügt.";
                        }
                        JOptionPane.showMessageDialog(this, message);
                        // Navigate back to Order panel to see the newly added order
                        Container ancestor = SwingUtilities.getAncestorOfClass(MainFrame.class, this);
                        if (ancestor instanceof MainFrame) {
                            MainFrame mainFrameInstance = (MainFrame) ancestor;
                            mainFrameInstance.showPanel("Order");  // Use the existing showPanel method
                        }
                        
                        // Clear selections for next use
                        selectedProducts.clear();
                        tableModel.fireTableDataChanged();
                        // No need to update total label because we're navigating away
                        return;
                    } catch (Exception ex) {
                        ex.printStackTrace();
                        JOptionPane.showMessageDialog(this, "Error saving order: " + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
                    }
                } else {
                    JOptionPane.showMessageDialog(this, "Alle Felder müssen ausgefüllt werden.",
                            "Fehlende Informationen", JOptionPane.WARNING_MESSAGE);
                }
            }
        }

                // Table model and renderers
                static class ProductTableModel extends AbstractTableModel {
                        private java.util.List<ProductInfo> data;
                        private Runnable updateTotalCallback;
                        private String[] componentTypes;

                        ProductTableModel(java.util.List<ProductInfo> data, String[] componentTypes) {
                                this.data = data;
                                this.componentTypes = componentTypes;
                        }

                        public void setUpdateTotalCallback(Runnable cb) {
                                this.updateTotalCallback = cb;
                        }

                        public int getRowCount() {
                                return componentTypes.length;
                        }

                        public int getColumnCount() {
                                return COLUMN_NAMES.length;
                        }

                        public String getColumnName(int col) {
                                return COLUMN_NAMES[col];
                        }

                        public Object getValueAt(int row, int col) {
            ProductInfo info = data.get(row);
            if (info == null) {
                switch (col) {
                    case 0:
                        return componentTypes[row]; // Always show type
                    case 1:
                    case 2:
                    case 4:
                    case 5:
                        return "";
                    case 3:
                    case 6:
                        return 0;
                    case 7:
                        return "edit";
                }
            } else {
                switch (col) {
                    case 0:
                        return info.type != null ? info.type : "";
                    case 1:
                        return info.name != null ? info.name : "";
                    case 2:
                        return info.manufacturer != null ? info.manufacturer : "";
                    case 3:
                        return info.itemCount;
                    case 4:
                        return info.details != null ? info.details : "";
                    case 5:
                        return info.availability != null ? info.availability : "";
                    case 6:
                        return String.format("$%.2f", info.price);
                    case 7:
                        return "edit";
                }
            }
            return null;
        }

                        public void setValueAt(Object value, int row, int col) {
                                if (col == 1 && value instanceof ProductInfo) {
                                        data.set(row, (ProductInfo) value);
                                        if (updateTotalCallback != null)
                                                updateTotalCallback.run();
                                        fireTableRowsUpdated(row, row);
                                }
                        }

                        public boolean isCellEditable(int row, int col) {
                                return col == 7;
                        }
                }

                static class DetailsCellRenderer extends JTextArea implements TableCellRenderer {
                        DetailsCellRenderer() {
                                setLineWrap(true);
                                setWrapStyleWord(true);
                                setOpaque(true);
                                setFont(new Font("Arial", Font.PLAIN, 13));
                                setBorder(null);
                                setMargin(new Insets(4, 4, 4, 4));
                        }

                        public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected,
                                        boolean hasFocus, int row, int column) {
                                setText(value != null ? value.toString() : "");
                                setBackground(isSelected ? table.getSelectionBackground() : Color.WHITE);
                                setForeground(Color.BLACK);
                                return this;
                        }
                }

                static class AvailabilityCellRenderer extends DefaultTableCellRenderer {
                        @Override
                        public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected,
                                        boolean hasFocus, int row, int column) {
                                JPanel panel = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 0));
                                panel.setOpaque(true);
                                panel.setBackground(isSelected ? table.getSelectionBackground() : Color.WHITE);
                                String text = value != null ? value.toString() : "";
                                JLabel dot = new JLabel("●");
                                if (text.contains("Im Lager"))
                                        dot.setForeground(new Color(76, 175, 80));
                                else if (text.contains("wenige"))
                                        dot.setForeground(new Color(255, 152, 0));
                                else
                                        dot.setForeground(new Color(244, 67, 54));
                                JLabel label = new JLabel(text);
                                label.setFont(new Font("Arial", Font.PLAIN, 13));
                                panel.add(dot);
                                panel.add(label);
                                return panel;
                        }
                }

                static class EditIconRenderer extends DefaultTableCellRenderer {
                        @Override
                        public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected,
                                        boolean hasFocus, int row, int column) {
                                JLabel label = new JLabel("Choose");
                                label.setHorizontalAlignment(SwingConstants.CENTER);
                                label.setFont(new Font("Arial", Font.PLAIN, 12));
                                label.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
                                label.setOpaque(true);
                                label.setBackground(isSelected ? table.getSelectionBackground() : Color.WHITE);
                                label.setForeground(new Color(0, 102, 204)); // Blue color for button text
                                label.setBorder(BorderFactory.createRaisedBevelBorder());
                                return label;
                        }
                }

                static class EditIconEditor extends AbstractCellEditor implements TableCellEditor {
                        private JButton button = new JButton("Choose");
                        private PCBuilderPanel parent;
                        private int editingRow = -1;
                        private String[] componentTypes;

                        EditIconEditor(PCBuilderPanel parent, String[] componentTypes) {
                                this.parent = parent;
                                this.componentTypes = componentTypes;
                                button.setFont(new Font("Arial", Font.PLAIN, 12));
                                button.setBorderPainted(true);
                                button.setContentAreaFilled(true);
                                button.setFocusPainted(false);
                                button.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
                                button.setForeground(new Color(0, 102, 204)); // Blue color for button text
                                button.addActionListener(e -> {
                                        fireEditingStopped();
                                        int row = editingRow;
                                        String type = componentTypes[row];
                                        java.util.List<ProductInfo> products = COMPONENTS.get(type);
                                        if (products == null) products = new java.util.ArrayList<>();
                                        if (products.isEmpty()) {
                                                JOptionPane.showMessageDialog(parent, "Dieser Artikel ist nicht verfügbar. Bitte wählen Sie ein anderes Produkt.", "Artikel nicht verfügbar", JOptionPane.WARNING_MESSAGE);
                                                return;
                                        }
                                        // Fix: Pass Window ancestor, not panel, to dialog
                                        java.awt.Window window = javax.swing.SwingUtilities.getWindowAncestor(parent);
                                        ProductSelectionDialog dialog = new ProductSelectionDialog(window, type, products);
                                        ProductInfo selected = dialog.showDialog();
                                        if (selected != null) {
                                                if (selected.itemCount <= 0) {
                                                    JOptionPane.showMessageDialog(parent, "Dieser Artikel ist nicht verfügbar. Bitte wählen Sie ein anderes Produkt.", "Artikel nicht verfügbar", JOptionPane.WARNING_MESSAGE);
                                                    return;
                                                }
                                                
                                                // Check compatibility before allowing selection
                                                if (!checkComponentCompatibility(parent, selected, row)) {
                                                    return; // User cancelled due to compatibility issue
                                                }
                                                
                                                // Always set item count to 1 after selection
                                                selected.itemCount = 1;
                                                parent.selectedProducts.set(row, selected);
                                                parent.tableModel.fireTableRowsUpdated(row, row);
                                                if (parent.tableModel.updateTotalCallback != null) parent.tableModel.updateTotalCallback.run();
                                        }
                                });
                        }

                        @Override
                        public Component getTableCellEditorComponent(JTable table, Object value, boolean isSelected,
                                        int row, int column) {
                                editingRow = row;
                                return button;
                        }

                        @Override
                        public Object getCellEditorValue() {
                                return null;
                        }
                        
                        /**
                         * Check compatibility of the newly selected component with existing selections
                         * @param parent PCBuilderPanel reference
                         * @param newSelection The newly selected ProductInfo
                         * @param row The row being edited
                         * @return true if compatible or user chooses to continue, false if cancelled
                         */
                        private boolean checkComponentCompatibility(PCBuilderPanel parent, ProductInfo newSelection, int row) {
                            // Get current selections
                            java.util.List<ProductInfo> currentSelections = new java.util.ArrayList<>();
                            for (int i = 0; i < parent.selectedProducts.size(); i++) {
                                if (i == row) {
                                    // Use the new selection for this row
                                    currentSelections.add(newSelection);
                                } else {
                                    // Use existing selections for other rows
                                    currentSelections.add(parent.selectedProducts.get(i));
                                }
                            }
                            
                            // Find processor and motherboard in selections
                            ProductInfo processor = null;
                            ProductInfo motherboard = null;
                            
                            for (ProductInfo component : currentSelections) {
                                if (component == null) continue;
                                
                                String type = component.type;
                                if (type != null) {
                                    if (type.toLowerCase().contains("processor") || type.toLowerCase().contains("cpu")) {
                                        processor = component;
                                    } else if (type.toLowerCase().contains("motherboard")) {
                                        motherboard = component;
                                    }
                                }
                            }
                            
                            // Check compatibility only if we have both processor and motherboard
                            if (processor != null && motherboard != null) {
                                return checkProcessorMotherboardCompatibility(processor, motherboard);
                            }
                            
                            return true; // No compatibility issues if we don't have both components
                        }
                        
                        /**
                         * Check processor-motherboard compatibility using manufacturer rules
                         */
                        private boolean checkProcessorMotherboardCompatibility(ProductInfo processor, ProductInfo motherboard) {
                            if (processor == null || motherboard == null) {
                                return true; // Allow if either is null
                            }
                            
                            String processorManufacturer = processor.manufacturer;
                            String motherboardManufacturer = motherboard.manufacturer;
                            
                            if (processorManufacturer == null || motherboardManufacturer == null) {
                                return true; // Allow if manufacturer info missing
                            }
                            
                            String cpuManu = processorManufacturer.toLowerCase();
                            String mbManu = motherboardManufacturer.toLowerCase();
                            
                            String incompatibilityMessage = null;
                            
                            // Intel processor compatibility rules
                            if (cpuManu.contains("intel")) {
                                if (mbManu.contains("gigabyte")) {
                                    incompatibilityMessage = 
                                        "Compatibility Issue\n\n" +
                                        "Intel processors are not compatible with Gigabyte motherboards.\n" +
                                        "Intel processors work with: ASRock, ASUS, MSI\n\n" +
                                        "Current selection:\n" +
                                        "- Processor: " + processor.name + " (" + processor.manufacturer + ")\n" +
                                        "- Motherboard: " + motherboard.name + " (" + motherboard.manufacturer + ")\n\n" +
                                        "Would you like to continue anyway?";
                                }
                            }
                            
                            // AMD processor compatibility rules  
                            if (cpuManu.contains("amd")) {
                                if (mbManu.contains("asus")) {
                                    incompatibilityMessage =
                                        "Compatibility Issue\n\n" +
                                        "AMD processors are not compatible with ASUS motherboards.\n" +
                                        "AMD processors work with: Gigabyte, ASRock, MSI\n\n" +
                                        "Current selection:\n" +
                                        "- Processor: " + processor.name + " (" + processor.manufacturer + ")\n" +
                                        "- Motherboard: " + motherboard.name + " (" + motherboard.manufacturer + ")\n\n" +
                                        "Would you like to continue anyway?";
                                }
                            }
                            
                            // If there's an incompatibility, show warning dialog
                            if (incompatibilityMessage != null) {
                                int choice = JOptionPane.showConfirmDialog(
                                    parent,
                                    incompatibilityMessage,
                                    "Component Compatibility Warning",
                                    JOptionPane.YES_NO_OPTION,
                                    JOptionPane.WARNING_MESSAGE
                                );
                                
                                return choice == JOptionPane.YES_OPTION; // Return true only if user chooses to continue
                            }
                            
                            return true; // Compatible
                        }
                }

                static class ProductInfo {
                        String type, name, manufacturer, details, availability;
                        int itemCount;
                        double price;

                        public ProductInfo(String type, String name, String manufacturer, int itemCount, String details,
                                        String availability, double price) {
                                this.type = type;
                                this.name = name;
                                this.manufacturer = manufacturer;
                                this.itemCount = itemCount;
                                this.details = details;
                                this.availability = availability;
                                this.price = price;
                        }

                        @Override
                        public String toString() {
                                // Only show name, manufacturer, and price (no type prefix)
                                return name + " (" + manufacturer + ") - $" + String.format("%.2f", price);
                        }
                }

                //RangeSlider für Preisbereich
                static class RangeSlider extends JComponent {
                    private int min, max, low, high;
                    private boolean draggingLow = false, draggingHigh = false;
                    private static final int THUMB_SIZE = 16;

                    public RangeSlider(int min, int max, int low, int high) {
                        this.min = min; this.max = max; this.low = low; this.high = high;
                        setPreferredSize(new Dimension(180, 32));
                        MouseAdapter ma = new MouseAdapter() {
                            public void mousePressed(MouseEvent e) {
                                int lx = valueToX(RangeSlider.this.low), hx = valueToX(RangeSlider.this.high);
                                if (Math.abs(e.getX() - lx) < THUMB_SIZE) {
                                    draggingLow = true;
                                } else if (Math.abs(e.getX() - hx) < THUMB_SIZE) {
                                    draggingHigh = true;
                                }
                            }
                            public void mouseReleased(MouseEvent e) { draggingLow = draggingHigh = false; }
                            public void mouseDragged(MouseEvent e) {
                                int v = xToValue(e.getX());
                                if (draggingLow) {
                                    RangeSlider.this.low = Math.max(min, Math.min(v, RangeSlider.this.high));
                                    firePropertyChange("low", 0, RangeSlider.this.low);
                                    repaint();
                                } else if (draggingHigh) {
                                    RangeSlider.this.high = Math.min(max, Math.max(v, RangeSlider.this.low));
                                    firePropertyChange("high", 0, RangeSlider.this.high);
                                    repaint();
                                }
                            }
                        };
                        addMouseListener(ma); addMouseMotionListener(ma);
                    }
                    public int getLow() { return low; }
                    public int getHigh() { return high; }
                    public void setLow(int l) { low = Math.max(min, Math.min(l, high)); repaint(); }
                    public void setHigh(int h) { high = Math.min(max, Math.max(h, low)); repaint(); }
                    private int valueToX(int v) {
                        int w = getWidth() - 2 * THUMB_SIZE;
                        return THUMB_SIZE + (int)((v - min) * w / (double)(max - min));
                    }
                    private int xToValue(int x) {
                        int w = getWidth() - 2 * THUMB_SIZE;
                        int v = min + (int)((x - THUMB_SIZE) * (max - min) / (double)w);
                        return Math.max(min, Math.min(max, v));
                    }
                    protected void paintComponent(Graphics g) {
                        super.paintComponent(g);
                        int w = getWidth(), h = getHeight();
                        int lx = valueToX(low), hx = valueToX(high);
                        g.setColor(Color.BLACK);
                        g.fillRect(THUMB_SIZE, h/2-2, w-2*THUMB_SIZE, 4);
                        g.setColor(Color.WHITE);
                        g.fillOval(lx-THUMB_SIZE/2, h/2-THUMB_SIZE/2, THUMB_SIZE, THUMB_SIZE);
                        g.fillOval(hx-THUMB_SIZE/2, h/2-THUMB_SIZE/2, THUMB_SIZE, THUMB_SIZE);
                        g.setColor(Color.BLACK);
                        g.drawOval(lx-THUMB_SIZE/2, h/2-THUMB_SIZE/2, THUMB_SIZE, THUMB_SIZE);
                        g.drawOval(hx-THUMB_SIZE/2, h/2-THUMB_SIZE/2, THUMB_SIZE, THUMB_SIZE);
                    }
                }

                // --- NEU: ProductSelectionDialog für gefilterte Produktauswahl ---
                static class ProductSelectionDialog extends JDialog {
                    private JComboBox<PCBuilderPanel.ProductInfo> productComboBox;
                    private JComboBox<String> manufacturerBox;
                    private JToggleButton inStockButton, fewAvailableButton;
                    private JTextField priceMinField, priceMaxField;
                    private RangeSlider priceSlider;
                    private java.util.List<PCBuilderPanel.ProductInfo> allProducts;
                    private java.util.List<PCBuilderPanel.ProductInfo> filteredProducts;
                    private PCBuilderPanel.ProductInfo selectedProduct;
                    private int minPrice, maxPrice;

                    public ProductSelectionDialog(Window parent, String type, java.util.List<PCBuilderPanel.ProductInfo> products) {
                        super(parent, "Select " + type, ModalityType.APPLICATION_MODAL);
                        this.allProducts = new java.util.ArrayList<>(products);
                        this.filteredProducts = new java.util.ArrayList<>(products);
                        setLayout(new BorderLayout(10, 0));
                        setSize(870, 320); // Increase window width to accommodate larger dropdown and left margin
                        setLocationRelativeTo(parent); // Recenter dialog

                        // Preisbereich bestimmen
                        minPrice = (int) Math.floor(allProducts.stream().mapToDouble(p -> p.price).min().orElse(0));
                        maxPrice = (int) Math.ceil(allProducts.stream().mapToDouble(p -> p.price).max().orElse(1000));

                        // Links: Produktliste (Dropdown)
                        JPanel leftPanel = new JPanel();
                        leftPanel.setLayout(new BoxLayout(leftPanel, BoxLayout.Y_AXIS));
                        leftPanel.setBorder(BorderFactory.createEmptyBorder(30, 60, 30, 10)); // Add more space to the left
                        leftPanel.setPreferredSize(new Dimension(480, 200)); // Add width to the left
                        leftPanel.setMaximumSize(new Dimension(480, Integer.MAX_VALUE));
                        JLabel selectLabel = new JLabel("Select " + type + ":");
                        selectLabel.setAlignmentX(Component.LEFT_ALIGNMENT);
                        leftPanel.add(selectLabel);
                        leftPanel.add(Box.createVerticalStrut(8));
                        productComboBox = new JComboBox<>();
                        productComboBox.setPreferredSize(new Dimension(440, 64));
                        productComboBox.setMaximumSize(new Dimension(440, 64));
                        productComboBox.setAlignmentX(Component.LEFT_ALIGNMENT);
                        leftPanel.add(productComboBox);
                        leftPanel.add(Box.createVerticalGlue());

                        // Populate the combo box with all products for this type
                        System.out.println("[DEBUG] Opening ProductSelectionDialog for type: " + type);
                        System.out.println("[DEBUG] Products passed to dialog: " + products.size());
                        for (PCBuilderPanel.ProductInfo p : products) {
                            System.out.println("[DEBUG]   - " + p.name + " (" + p.manufacturer + ", " + p.price + ")");
                        }
                        updateProductComboBox();

                        // Rechts: Filter-Panel
                        JPanel filterPanel = new JPanel();
                        filterPanel.setLayout(new BoxLayout(filterPanel, BoxLayout.Y_AXIS));
                        filterPanel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));
                        // Hersteller
                        JLabel manuLabel = new JLabel("Manufacturer:");
                        manuLabel.setAlignmentX(Component.LEFT_ALIGNMENT);
                        filterPanel.add(manuLabel);
                        filterPanel.add(Box.createVerticalStrut(4));
                        // Only use manufacturers from the products for the current component type
                        java.util.Set<String> manufacturers = new java.util.TreeSet<>();
                        for (PCBuilderPanel.ProductInfo p : products) manufacturers.add(p.manufacturer);
                        manufacturerBox = new JComboBox<>();
                        manufacturerBox.addItem("All");
                        for (String m : manufacturers) manufacturerBox.addItem(m);
                        manufacturerBox.setMaximumSize(new Dimension(180, 28));
                        manufacturerBox.setAlignmentX(Component.LEFT_ALIGNMENT);
                        filterPanel.add(manufacturerBox);
                        filterPanel.add(Box.createVerticalStrut(16));
                        // Availability
                        JLabel availLabel = new JLabel("Availability:");
                        availLabel.setAlignmentX(Component.LEFT_ALIGNMENT);
                        filterPanel.add(availLabel);
                        filterPanel.add(Box.createVerticalStrut(4));
                        JPanel availPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 5, 0));
                        inStockButton = new JToggleButton("Im Lager");
                        fewAvailableButton = new JToggleButton("wenige Verfügbar");
                        inStockButton.setSelected(true);
                        fewAvailableButton.setSelected(true);
                        availPanel.add(inStockButton);
                        availPanel.add(fewAvailableButton);
                        availPanel.setAlignmentX(Component.LEFT_ALIGNMENT);
                        filterPanel.add(availPanel);
                        filterPanel.add(Box.createVerticalStrut(16));
                        // Preisbereich
                        JLabel priceLabel = new JLabel("Preis");
                        priceLabel.setAlignmentX(Component.LEFT_ALIGNMENT);
                        filterPanel.add(priceLabel);
                        filterPanel.add(Box.createVerticalStrut(4));
                        JPanel priceFieldPanel = new JPanel();
                        priceFieldPanel.setLayout(new BoxLayout(priceFieldPanel, BoxLayout.X_AXIS));
                        priceMinField = new JTextField(String.valueOf(minPrice));
                        priceMaxField = new JTextField(String.valueOf(maxPrice));
                        priceMinField.setMaximumSize(new Dimension(60, 28));
                        priceMaxField.setMaximumSize(new Dimension(60, 28));
                        priceFieldPanel.add(new JLabel("Von (€) "));
                        priceFieldPanel.add(priceMinField);
                        priceFieldPanel.add(Box.createHorizontalStrut(10));
                        priceFieldPanel.add(new JLabel("Bis (€) "));
                        priceFieldPanel.add(priceMaxField);
                        priceFieldPanel.setAlignmentX(Component.LEFT_ALIGNMENT);
                        filterPanel.add(priceFieldPanel);
                        filterPanel.add(Box.createVerticalStrut(8));
                        priceSlider = new RangeSlider(minPrice, maxPrice, minPrice, maxPrice);
                        priceSlider.setAlignmentX(Component.LEFT_ALIGNMENT);
                        filterPanel.add(priceSlider);
                        filterPanel.add(Box.createVerticalGlue());

                        // Synchronisation Felder <-> Slider
                        priceSlider.addPropertyChangeListener(evt -> {
                            if ("low".equals(evt.getPropertyName())) {
                                priceMinField.setText(String.valueOf(priceSlider.getLow()));
                                applyFilters();
                            } else if ("high".equals(evt.getPropertyName())) {
                                priceMaxField.setText(String.valueOf(priceSlider.getHigh()));
                                applyFilters();
                            }
                        });
                        priceMinField.addActionListener(e -> {
                            try {
                                int v = Integer.parseInt(priceMinField.getText());
                                priceSlider.setLow(v);
                                applyFilters();
                            } catch (Exception ex) {}
                        });
                        priceMaxField.addActionListener(e -> {
                            try {
                                int v = Integer.parseInt(priceMaxField.getText());
                                priceSlider.setHigh(v);
                                applyFilters();
                            } catch (Exception ex) {}
                        });

                        // Filter-Listener
                        java.awt.event.ActionListener filterListener = e -> applyFilters();
                        manufacturerBox.addActionListener(filterListener);
                        inStockButton.addActionListener(filterListener);
                        fewAvailableButton.addActionListener(filterListener);

                        // Buttons unten
                        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
                        JButton okBtn = new JButton("OK");
                        JButton cancelBtn = new JButton("Cancel");
                        okBtn.addActionListener(e -> {
                            selectedProduct = (PCBuilderPanel.ProductInfo) productComboBox.getSelectedItem();
                            dispose();
                        });
                        cancelBtn.addActionListener(e -> {
                            selectedProduct = null;
                            dispose();
                        });
                        buttonPanel.add(okBtn);
                        buttonPanel.add(cancelBtn);

                        add(leftPanel, BorderLayout.CENTER);
                        add(filterPanel, BorderLayout.EAST);
                        add(buttonPanel, BorderLayout.SOUTH);
                        updateProductComboBox();
                    }

                    private void updateProductComboBox() {
                        productComboBox.removeAllItems();
                        for (PCBuilderPanel.ProductInfo p : filteredProducts) {
                            productComboBox.addItem(p);
                        }
                        if (productComboBox.getItemCount() > 0) {
                            productComboBox.setSelectedIndex(0);
                        }
                    }

                    private void applyFilters() {
                        String selectedManu = (String) manufacturerBox.getSelectedItem();
                        boolean filterInStock = inStockButton.isSelected();
                        boolean filterFew = fewAvailableButton.isSelected();
                        int min = priceSlider.getLow();
                        int max = priceSlider.getHigh();
                        filteredProducts.clear();
                        // Use only the products for this component type
                        for (PCBuilderPanel.ProductInfo p : allProducts) {
                            // Hersteller
                            if (!selectedManu.equals("All") && !p.manufacturer.equals(selectedManu)) continue;
                            // Availability
                            boolean availMatch = false;
                            if (!filterInStock && !filterFew) availMatch = true; // Both off: show all
                            else if (filterInStock && filterFew) availMatch = true; // Both on: show all
                            else if (filterInStock && p.availability.contains("Im Lager")) availMatch = true;
                            else if (filterFew && p.availability.contains("wenige Verfügbar")) availMatch = true;
                            else if (filterFew && p.availability.contains("Nicht verfügbar")) availMatch = true;
                            if (!availMatch) continue;
                            // Preis
                            double price = p.price;
                            if (price < min || price > max) continue;
                            filteredProducts.add(p);
                        }
                        updateProductComboBox();
                    }

                    public PCBuilderPanel.ProductInfo showDialog() {
                        setVisible(true);
                        return selectedProduct;
                    }
                }
        }

        // Order panel with enhanced styling
        static class OrderPanel extends JPanel {
                private JTable table;
                public DefaultTableModel tableModel;
                private JTextField orderIdField, dateField;
                private JComboBox<String> statusBox;
                private OrderDAO orderDAO;

                OrderPanel() {
                        this.orderDAO = new OrderDAO();
                        setLayout(new BorderLayout());
                        setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));
                        
                        // Create a combined north panel for header and search
                        JPanel northPanel = new JPanel(new BorderLayout());
                        
                        // Header panel (title + orange button)
                        JPanel headerPanel = new JPanel(new BorderLayout());
                        JPanel titlePanel = new JPanel(new FlowLayout(FlowLayout.CENTER));
                        JLabel titleLabel = new JLabel("Order List");
                        titleLabel.setFont(new Font("Arial", Font.BOLD, 20));
                        titlePanel.add(titleLabel);
                        headerPanel.add(titlePanel, BorderLayout.CENTER);
                        JButton addButton = new JButton("Create Order");
                        addButton.setBackground(new Color(219, 68, 23));
                        addButton.setForeground(Color.WHITE);
                        addButton.setFocusPainted(false);
                        headerPanel.add(addButton, BorderLayout.EAST);
                        
                        // Search panel
                        JPanel searchPanel = new JPanel();
                        searchPanel.setLayout(new BoxLayout(searchPanel, BoxLayout.Y_AXIS));
                        searchPanel.setBorder(BorderFactory.createCompoundBorder(
                                        BorderFactory.createMatteBorder(0, 0, 1, 0, new Color(220, 220, 220)),
                                        BorderFactory.createEmptyBorder(10, 10, 10, 10))); // Reduced bottom padding from 0 to 10
                        JPanel filterPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 15, 5));
                        // Order ID
                        JPanel orderIdPanel = new JPanel(new BorderLayout(5, 0));
                        JLabel orderIdLabel = new JLabel("Order ID");
                        orderIdLabel.setFont(new Font("Arial", Font.PLAIN, 12));
                        orderIdField = new JTextField(15);
                        orderIdField.setPreferredSize(new Dimension(orderIdField.getPreferredSize().width, 30));
                        orderIdPanel.add(orderIdLabel, BorderLayout.NORTH);
                        orderIdPanel.add(orderIdField, BorderLayout.CENTER);
                        filterPanel.add(orderIdPanel);
                        // Status
                        JPanel statusPanel = new JPanel(new BorderLayout(5, 0));
                        JLabel statusLabel = new JLabel("Status");
                        statusLabel.setFont(new Font("Arial", Font.PLAIN, 12));
                        statusBox = new JComboBox<>(new String[] {
                                        "All", "Neue Bestellung", "In Bearbeitung", "Komponente wird nachbestellt",
                                        "Abholbereit", "Im Lager", "Abgeschlossen"
                        });
                        statusBox.setPreferredSize(new Dimension(200, 30));
                        statusPanel.add(statusLabel, BorderLayout.NORTH);
                        statusPanel.add(statusBox, BorderLayout.CENTER);
                        filterPanel.add(statusPanel);
                        // Date
                        JPanel datePanel = new JPanel(new BorderLayout(5, 0));
                        JLabel dateLabel = new JLabel("Date");
                        dateLabel.setFont(new Font("Arial", Font.PLAIN, 12));
                        dateField = new JTextField(15);
                        dateField.setPreferredSize(new Dimension(dateField.getPreferredSize().width, 30));
                        datePanel.add(dateLabel, BorderLayout.NORTH);
                        datePanel.add(dateField, BorderLayout.CENTER);
                        filterPanel.add(datePanel);
                        // Calendar button
                        JButton calendarButton = new JButton("📅");
                        calendarButton.setMargin(new Insets(2, 2, 2, 2));
                        calendarButton.setFocusable(false);
                        calendarButton.setToolTipText("Tag auswählen");
                        calendarButton.addActionListener(e -> {
                                CalendarDialog dialog = new CalendarDialog((Frame) SwingUtilities.getWindowAncestor(this));
                                java.util.Date selected = dialog.showDialog();
                                if (selected != null) {
                                        java.text.SimpleDateFormat sdf = new java.text.SimpleDateFormat("dd.MM.yyyy");
                                        dateField.setText(sdf.format(selected));
                                }
                        });
                        JPanel dateFieldPanel = new JPanel(new BorderLayout());
                        dateFieldPanel.add(dateField, BorderLayout.CENTER);
                        dateFieldPanel.add(calendarButton, BorderLayout.EAST);
                        datePanel.remove(dateField);
                        datePanel.add(dateFieldPanel, BorderLayout.CENTER);
                        // Search button
                        JPanel searchButtonPanel = new JPanel(new BorderLayout());
                        searchButtonPanel.setBorder(BorderFactory.createEmptyBorder(12, 0, 0, 0));
                        JButton searchButton = new JButton("Search");
                        searchButton.setBackground(new Color(51, 122, 220));
                        searchButton.setForeground(Color.WHITE);
                        searchButton.setFocusPainted(false);
                        searchButton.setPreferredSize(new Dimension(100, 30));
                        searchButtonPanel.add(searchButton, BorderLayout.CENTER);
                        filterPanel.add(searchButtonPanel);
                        searchPanel.add(filterPanel);
                        
                        // Add header and search to north panel
                        northPanel.add(headerPanel, BorderLayout.NORTH);
                        northPanel.add(searchPanel, BorderLayout.CENTER);
                        
                        // Add the combined north panel
                        add(northPanel, BorderLayout.NORTH);
                        // Table
                        String[] columns = { "Order ID", "Order Number", "Customer Name", "Status", "Date", "Invoice ID", "Einsicht" };
                        tableModel = new DefaultTableModel(new Object[][]{}, columns) {
                                @Override
                                public boolean isCellEditable(int row, int column) {
                                        // Only the last column (Einsicht) is editable for the button
                                        return column == 6;
                                }
                        };
                        table = new JTable(tableModel);
                        styleTable(table);
                        // Add button renderer/editor for Einsicht
                        javax.swing.table.TableColumn einsichtCol = table.getColumnModel().getColumn(6);
                        einsichtCol.setCellRenderer(new javax.swing.table.DefaultTableCellRenderer() {
                            @Override
                            public java.awt.Component getTableCellRendererComponent(javax.swing.JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
                                javax.swing.JButton button = new javax.swing.JButton("Einsicht");
                                button.setBackground(new java.awt.Color(51, 122, 220));
                                button.setForeground(java.awt.Color.WHITE);
                                button.setFocusPainted(false);
                                button.setBorderPainted(false);
                                button.setOpaque(true);
                                return button;
                            }
                        });
                        einsichtCol.setCellEditor(new javax.swing.DefaultCellEditor(new javax.swing.JTextField()) {
                            private javax.swing.JButton button = new javax.swing.JButton("Einsicht");
                            private int editingRow = -1;
                            {
                                button.setBackground(new java.awt.Color(51, 122, 220));
                                button.setForeground(java.awt.Color.WHITE);
                                button.setFocusPainted(false);
                                button.setBorderPainted(false);
                                button.setOpaque(true);
                                button.addActionListener(e -> {
                                    editingRow = table.getSelectedRow();
                                    if (editingRow >= 0) {
                                        int orderId = Integer.parseInt(tableModel.getValueAt(editingRow, 0).toString());
                                        showOrderDetailDialog(orderId);
                                    }
                                });
                            }
                            @Override
                            public java.awt.Component getTableCellEditorComponent(javax.swing.JTable table, Object value, boolean isSelected, int row, int column) {
                                editingRow = row;
                                return button;
                            }
                            @Override
                            public Object getCellEditorValue() {
                                return "Einsicht";
                            }
                        });
                        JScrollPane scrollPane = new JScrollPane(table);
                        scrollPane.setBorder(BorderFactory.createEmptyBorder());
                        // Add the table in the center so it takes up the remaining space
                        add(scrollPane, BorderLayout.CENTER);

                        loadOrderData(); // Load data from DB

                        // Search action
                        searchButton.addActionListener(e -> {
                                filterOrderData(); // Filter data based on search fields
                        });

                        // Add button action
                        addButton.addActionListener(e -> {
                                // Open PC Builder Dialog instead of CreateOrderFrame
                                System.out.println("[DEBUG] OrderPanel CREATE ORDER button clicked");
                                try {
                                        System.out.println("[DEBUG] Creating PCBuilderDialog from OrderPanel...");
                                        JFrame parentFrame = (JFrame) SwingUtilities.getWindowAncestor(OrderPanel.this);
                                        System.out.println("[DEBUG] Parent frame: " + parentFrame);
                                        PCBuilderDialog dialog = new PCBuilderDialog(
                                                parentFrame,
                                                new OrderCreationListener() {
                                                        @Override
                                                        public void addCustomer(String id, String name, String email, String phone) {
                                                                // Add customer if needed
                                                        }
                                                        
                                                        @Override
                                                        public void addOrder(String orderId, String orderNumber, String status, String customerName, 
                                                                        String date, String invoiceId, java.util.List<PCBuilderPanel.ProductInfo> selectedProducts) {
                                                                // Dialog will refresh order list when closed
                                                        }
                                                });
                                        System.out.println("[DEBUG] PCBuilderDialog created, showing...");
                                        dialog.setVisible(true);
                                        System.out.println("[DEBUG] PCBuilderDialog setVisible(true) called");
                                } catch (Exception ex) {
                                        System.err.println("[ERROR] Exception in OrderPanel CREATE ORDER: " + ex.getMessage());
                                        ex.printStackTrace();
                                }
                        });
                }

                public void loadOrderData() {
                        tableModel.setRowCount(0); // Clear existing data
                        List<clearorder.model.Order> orders = orderDAO.getAllOrders();
                        for (clearorder.model.Order order : orders) {
                                tableModel.addRow(new Object[]{
                                        String.valueOf(order.getOrderId()),
                                        order.getOrderNumber(), // Show just the number
                                        order.getCustomerName(),
                                        order.getStatus(),
                                        new SimpleDateFormat("dd.MM.yyyy").format(order.getOrderDate()),
                                        order.getInvoiceId() > 0 ? String.valueOf(order.getInvoiceId()) : "N/A",
                                        "✎"
                                });
                        }
                }

                private void filterOrderData() {
                        String orderId = orderIdField.getText().trim();
                        String status = (String) statusBox.getSelectedItem();
                        String date = dateField.getText().trim();
                        SimpleDateFormat sdf = new SimpleDateFormat("dd.MM.yyyy");

                        tableModel.setRowCount(0); // Clear existing data
                        List<clearorder.model.Order> orders = orderDAO.getAllOrders(); // Re-fetch all data to filter

                        for (clearorder.model.Order order : orders) {
                                boolean match = true;
                                if (!orderId.isEmpty() && !String.valueOf(order.getOrderId()).contains(orderId))
                                        match = false;
                                if (!date.isEmpty() && !sdf.format(order.getOrderDate()).contains(date))
                                        match = false;
                                if (!status.equals("All") && !order.getStatus().equals(status))
                                        match = false;

                                if (match)
                                        tableModel.addRow(new Object[]{
                                                        String.valueOf(order.getOrderId()),
                                                        order.getOrderNumber(),
                                                        order.getCustomerName(),
                                                        order.getStatus(),
                                                        sdf.format(order.getOrderDate()),
                                                        order.getInvoiceId() > 0 ? String.valueOf(order.getInvoiceId()) : "N/A",
                                                        "✎"
                                        });
                        }
                }

                // Show order details dialog
                private void showOrderDetailDialog(int orderId) {
                    clearorder.model.Order order = orderDAO.getOrderById(orderId);
                    if (order == null) return;
                    clearorder.dao.CustomerDAO customerDAO = new clearorder.dao.CustomerDAO();
                    final clearorder.model.Customer customer;
                    {
                        clearorder.model.Customer found = null;
                        for (clearorder.model.Customer c : customerDAO.getAllCustomers()) {
                            if (c.getCustomerName().equals(order.getCustomerName())) {
                                found = c;
                                break;
                            }
                        }
                        customer = found;
                    }
                    // Use GridBagLayout for better control
                    JPanel panel = new JPanel(new java.awt.GridBagLayout());
                    panel.setBorder(javax.swing.BorderFactory.createEmptyBorder(18, 24, 18, 24)); // Add breathing room
                    java.awt.GridBagConstraints gbc = new java.awt.GridBagConstraints();
                    gbc.insets = new java.awt.Insets(4, 4, 4, 4); // Less vertical space
                    gbc.anchor = java.awt.GridBagConstraints.WEST;
                    gbc.gridx = 0; gbc.gridy = 0;
                    panel.add(new JLabel("Order ID:"), gbc);
                    gbc.gridx = 1;
                    panel.add(new JLabel(String.valueOf(order.getOrderId())), gbc);
                    gbc.gridx = 0; gbc.gridy++;
                    panel.add(new JLabel("Order Number:"), gbc);
                    gbc.gridx = 1;
                    panel.add(new JLabel(order.getOrderNumber()), gbc);
                    gbc.gridx = 0; gbc.gridy++;
                    panel.add(new JLabel("Customer Name:"), gbc);
                    gbc.gridx = 1;
                    panel.add(new JLabel(order.getCustomerName()), gbc);
                    gbc.gridx = 0; gbc.gridy++;
                    panel.add(new JLabel("Status:"), gbc);
                    gbc.gridx = 1;
                    panel.add(new JLabel(order.getStatus()), gbc);
                    gbc.gridx = 0; gbc.gridy++;
                    panel.add(new JLabel("Date:"), gbc);
                    gbc.gridx = 1;
                    panel.add(new JLabel(new java.text.SimpleDateFormat("dd.MM.yyyy").format(order.getOrderDate())), gbc);
                    gbc.gridx = 0; gbc.gridy++;
                    panel.add(new JLabel("Invoice ID:"), gbc);
                    gbc.gridx = 1;
                    panel.add(new JLabel(order.getInvoiceId() > 0 ? String.valueOf(order.getInvoiceId()) : "N/A"), gbc);
                    // Status dropdown
                    String[] statuses = {"Neue Bestellung", "In Bearbeitung", "Komponente wird nachbestellt", "Abholbereit", "Im Lager", "Abgeschlossen"};
                    JComboBox<String> statusBox = new JComboBox<>(statuses);
                    statusBox.setSelectedItem(order.getStatus());
                    gbc.gridx = 0; gbc.gridy++;
                    panel.add(new JLabel("Status ändern:"), gbc);
                    gbc.gridx = 1;
                    panel.add(statusBox, gbc);
                    
                    // Email text area (for Abholbereit status)
                    JPanel mailPanel = new JPanel(new BorderLayout(5, 5));
                    mailPanel.setBorder(javax.swing.BorderFactory.createEmptyBorder(10, 0, 0, 0));
                    mailPanel.setVisible("Abholbereit".equals(statusBox.getSelectedItem()));
                    
                    String mailText = "Betreff: Ihr konfigurierter PC ist abholbereit – HighSpeed Filiale Stuttgart\n\n" +
                            "Sehr geehrte*r " + (customer != null ? customer.getCustomerName() : "Kunde") + ",\n\n" +
                            "wir freuen uns, Ihnen mitteilen zu können, dass Ihr individuell konfigurierter PC ab sofort zur Abholung bereitsteht. Ihre Order ID lautet: " + order.getOrderId() + "\n\n" +
                            "Bitte holen Sie Ihr Gerät in unserer HighSpeed-Filiale Stuttgart zu unseren regulären Öffnungszeiten ab:\n\n" +
                            "Adresse:\nHighSpeed Stuttgart\nKönigstraße 45\n70173 Stuttgart\n\n" +
                            "Öffnungszeiten:\nMontag – Freitag: 10:00 – 18:30 Uhr\nSamstag: 10:00 – 16:00 Uhr\n\n" +
                            "Bitte bringen Sie zur Abholung einen gültigen Lichtbildausweis sowie ggf. Ihre Bestellbestätigung mit.\n\n" +
                            "Bei Rückfragen stehen wir Ihnen jederzeit gerne zur Verfügung.\n\n" +
                            "Mit freundlichen Grüßen\nIhr HighSpeed-Team Stuttgart";
                    
                    JTextArea mailArea = new JTextArea(mailText, 8, 40);
                    mailArea.setLineWrap(true);
                    mailArea.setWrapStyleWord(true);
                    mailArea.setEditable(false);
                    JScrollPane scrollPane = new JScrollPane(mailArea);
                    
                    JButton copyBtn = new JButton("Text kopieren");
                    copyBtn.addActionListener(e -> {
                        mailArea.selectAll();
                        mailArea.copy();
                        // Use panel as parent instead of dialog to avoid reference issues
                        JOptionPane.showMessageDialog(panel, "Text wurde in die Zwischenablage kopiert!", "Kopiert", JOptionPane.INFORMATION_MESSAGE);
                    });
                    
                    JPanel copyBtnPanel = new JPanel(new java.awt.FlowLayout(java.awt.FlowLayout.RIGHT));
                    copyBtnPanel.add(copyBtn);
                    
                    mailPanel.add(new JLabel("E-Mail-Vorlage:"), java.awt.BorderLayout.NORTH);
                    mailPanel.add(scrollPane, java.awt.BorderLayout.CENTER);
                    mailPanel.add(copyBtnPanel, java.awt.BorderLayout.SOUTH);
                    
                    gbc.gridx = 0; gbc.gridy++;
                    gbc.gridwidth = 2;
                    gbc.fill = java.awt.GridBagConstraints.BOTH;
                    gbc.weightx = 1.0;
                    gbc.weighty = 1.0;
                    panel.add(mailPanel, gbc);
                    gbc.weightx = 0.0;
                    gbc.weighty = 0.0;
                    gbc.fill = java.awt.GridBagConstraints.NONE;
                    gbc.gridwidth = 1;
                    
                    // Main dialog panel
                    JPanel mainPanel = new JPanel(new java.awt.BorderLayout());
                    mainPanel.add(panel, java.awt.BorderLayout.CENTER);
                    
                    // Dialog
                    javax.swing.JDialog dialog = new javax.swing.JDialog((java.awt.Frame) javax.swing.SwingUtilities.getWindowAncestor(this), "Order Details", true);
                    dialog.getContentPane().add(mainPanel);
                    int defaultHeight = 380;
                    int abholbereitHeight = 580; // Taller to accommodate email text
                    dialog.setSize(500, "Abholbereit".equals(statusBox.getSelectedItem()) ? abholbereitHeight : defaultHeight);
                    dialog.setLocationRelativeTo(this);
                    
                    // OK/Cancel buttons
                    JPanel buttonPanel = new JPanel(new java.awt.FlowLayout(java.awt.FlowLayout.CENTER));
                    
                    // Invoice button
                    JButton invoiceBtn = new JButton("Rechnung erstellen");
                    invoiceBtn.setBackground(new java.awt.Color(51, 122, 183));
                    invoiceBtn.setForeground(java.awt.Color.WHITE);
                    invoiceBtn.setFocusPainted(false);
                    invoiceBtn.addActionListener(ev -> {
                        InvoicePreviewDialog invoiceDialog = new InvoicePreviewDialog((JFrame) SwingUtilities.getWindowAncestor(this), order.getOrderId());
                        invoiceDialog.setVisible(true);
                    });
                    
                    JButton okBtn = new JButton("OK");
                    JButton cancelBtn = new JButton("Abbrechen");
                    
                    buttonPanel.add(invoiceBtn);
                    buttonPanel.add(okBtn);
                    buttonPanel.add(cancelBtn);
                    mainPanel.add(buttonPanel, java.awt.BorderLayout.SOUTH);
                    
                    // Show/hide email panel on status change
                    statusBox.addActionListener(e -> {
                        boolean isAbholbereit = "Abholbereit".equals(statusBox.getSelectedItem());
                        mailPanel.setVisible(isAbholbereit);
                        dialog.setSize(500, isAbholbereit ? abholbereitHeight : defaultHeight);
                    });
                    
                    okBtn.addActionListener(ev -> {
                        String newStatus = (String) statusBox.getSelectedItem();
                        if (!order.getStatus().equals(newStatus)) {
                            order.setStatus(newStatus);
                            orderDAO.updateOrder(order);
                            loadOrderData();
                        }
                        dialog.dispose();
                    });
                    
                    cancelBtn.addActionListener(ev -> dialog.dispose());
                    dialog.setVisible(true);
                }
        }

        // Customer panel with enhanced styling
        static class CustomerPanel extends JPanel {
                private JTable table;
                public DefaultTableModel tableModel;
                private JTextField idField, nameField, emailField;
                private CustomerDAO customerDAO;

                CustomerPanel() {
                        this.customerDAO = new CustomerDAO();
                        setLayout(new BorderLayout());
                        setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));
                        
                        // Create a combined north panel for header and search
                        JPanel northPanel = new JPanel(new BorderLayout());
                        
                        // Header panel
                        JPanel headerPanel = new JPanel(new BorderLayout());
                        JPanel titlePanel = new JPanel(new FlowLayout(FlowLayout.CENTER));
                        JLabel titleLabel = new JLabel("Customers");
                        titleLabel.setFont(new Font("Arial", Font.BOLD, 20));
                        titlePanel.add(titleLabel);
                        headerPanel.add(titlePanel, BorderLayout.CENTER);
                        JButton addButton = new JButton("Add Customer");
                        addButton.setBackground(new Color(219, 68, 23));
                        addButton.setForeground(Color.WHITE);
                        addButton.setFocusPainted(false);
                        headerPanel.add(addButton, BorderLayout.EAST);
                        
                        // Search panel
                        JPanel searchPanel = new JPanel();
                        searchPanel.setLayout(new BoxLayout(searchPanel, BoxLayout.Y_AXIS));
                        searchPanel.setBorder(BorderFactory.createCompoundBorder(
                                        BorderFactory.createMatteBorder(0, 0, 1, 0, new Color(220, 220, 220)),
                                        BorderFactory.createEmptyBorder(10, 10, 10, 10))); // Reduced bottom padding from 0 to 10
                        JPanel filterPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 15, 5));
                        // Customer ID
                        JPanel idPanel = new JPanel(new BorderLayout(5, 0));
                        JLabel idLabel = new JLabel("Customer ID");
                        idLabel.setFont(new Font("Arial", Font.PLAIN, 12));
                        idField = new JTextField(15);
                        idField.setPreferredSize(new Dimension(idField.getPreferredSize().width, 30));
                        idPanel.add(idLabel, BorderLayout.NORTH);
                        idPanel.add(idField, BorderLayout.CENTER);
                        filterPanel.add(idPanel);
                        // Customer Name
                        JPanel namePanel = new JPanel(new BorderLayout(5, 0));
                        JLabel nameLabel = new JLabel("Customer Name");
                        nameLabel.setFont(new Font("Arial", Font.PLAIN, 12));
                        nameField = new JTextField(15);
                        nameField.setPreferredSize(new Dimension(nameField.getPreferredSize().width, 30));
                        namePanel.add(nameLabel, BorderLayout.NORTH);
                        namePanel.add(nameField, BorderLayout.CENTER);
                        filterPanel.add(namePanel);
                        // E-Mail
                        JPanel emailPanel = new JPanel(new BorderLayout(5, 0));
                        JLabel emailLabel = new JLabel("E-Mail");
                        emailLabel.setFont(new Font("Arial", Font.PLAIN, 12));
                        emailField = new JTextField(15);
                        emailField.setPreferredSize(new Dimension(emailField.getPreferredSize().width, 30));
                        emailPanel.add(emailLabel, BorderLayout.NORTH);
                        emailPanel.add(emailField, BorderLayout.CENTER);
                        filterPanel.add(emailPanel);
                        // Search button
                        JPanel searchButtonPanel = new JPanel(new BorderLayout());
                        searchButtonPanel.setBorder(BorderFactory.createEmptyBorder(12, 0, 0, 0));
                        JButton searchButton = new JButton("Search");
                        searchButton.setBackground(new Color(51, 122, 220));
                        searchButton.setForeground(Color.WHITE);
                        searchButton.setFocusPainted(false);
                        searchButton.setPreferredSize(new Dimension(100, 30));
                        searchButtonPanel.add(searchButton, BorderLayout.CENTER);
                        filterPanel.add(searchButtonPanel);
                        searchPanel.add(filterPanel);
                        
                        // Add header and search to north panel
                        northPanel.add(headerPanel, BorderLayout.NORTH);
                        northPanel.add(searchPanel, BorderLayout.CENTER);
                        
                        // Add the combined north panel
                        add(northPanel, BorderLayout.NORTH);
                        // Table
                        String[] columns = { "Customer ID", "Customer Name", "E-Mail", "Address", "Telephone Number", "Einsicht" };
                        tableModel = new DefaultTableModel(new Object[][]{}, columns) {
                                @Override
                                public boolean isCellEditable(int row, int column) {
                                        // Only the last column (Einsicht) is editable for the button
                                        return column == 5;
                                }
                        };
                        table = new JTable(tableModel);
                        styleTable(table);
                        // Add button renderer/editor for Einsicht
                        javax.swing.table.TableColumn einsichtCol = table.getColumnModel().getColumn(5);
                        einsichtCol.setCellRenderer(new javax.swing.table.DefaultTableCellRenderer() {
                            @Override
                            public java.awt.Component getTableCellRendererComponent(javax.swing.JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
                                javax.swing.JButton button = new javax.swing.JButton("Einsicht");
                                button.setBackground(new java.awt.Color(51, 122, 220));
                                button.setForeground(java.awt.Color.WHITE);
                                button.setFocusPainted(false);
                                button.setBorderPainted(false);
                                button.setOpaque(true);
                                return button;
                            }
                        });
                        einsichtCol.setCellEditor(new javax.swing.DefaultCellEditor(new javax.swing.JTextField()) {
                            private javax.swing.JButton button = new javax.swing.JButton("Einsicht");
                            private int editingRow = -1;
                            {
                                button.setBackground(new java.awt.Color(51, 122, 220));
                                button.setForeground(java.awt.Color.WHITE);
                                button.setFocusPainted(false);
                                button.setBorderPainted(false);
                                button.setOpaque(true);
                                button.addActionListener(e -> {
                                    editingRow = table.getSelectedRow();
                                    if (editingRow >= 0) {
                                        int customerId = Integer.parseInt(tableModel.getValueAt(editingRow, 0).toString());
                                        showCustomerDetailDialog(customerId);
                                    }
                                });
                            }
                            @Override
                            public java.awt.Component getTableCellEditorComponent(javax.swing.JTable table, Object value, boolean isSelected, int row, int column) {
                                editingRow = row;
                                return button;
                            }
                            @Override
                            public Object getCellEditorValue() {
                                return "Einsicht";
                            }
                        });
                        JScrollPane scrollPane = new JScrollPane(table);
                        scrollPane.setBorder(BorderFactory.createEmptyBorder());
                        // Add the table in the center so it takes up the remaining space
                        add(scrollPane, BorderLayout.CENTER);

                        loadCustomerData(); // Load data from DB

                        // Search action
                        searchButton.addActionListener(e -> {
                                filterCustomerData(); // Filter data based on search fields
                        });

                        // Add button action - this will be handled in AddCustomerFrame directly
                        addButton.addActionListener(e -> {
                                showAddCustomerDialog(CustomerPanel.this);
                                // No need to call loadCustomerData() as it's handled in the dialog
                        });
                }

                public void loadCustomerData() {
                        tableModel.setRowCount(0); // Clear existing data
                        List<clearorder.model.Customer> customers = customerDAO.getAllCustomers();
                        for (clearorder.model.Customer customer : customers) {
                                tableModel.addRow(new Object[]{
                                                String.valueOf(customer.getCustomerId()),
                                                customer.getCustomerName(),
                                                customer.getEmail(),
                                                customer.getAddress(),
                                                customer.getPhone(),
                                                "✎"
                                });
                        }
                }

                private void filterCustomerData() {
                        String id = idField.getText().trim();
                        String name = nameField.getText().trim().toLowerCase();
                        String email = emailField.getText().trim().toLowerCase();

                        tableModel.setRowCount(0); // Clear existing data
                        List<clearorder.model.Customer> customers = customerDAO.getAllCustomers(); // Re-fetch all data to filter

                        for (clearorder.model.Customer customer : customers) {
                                boolean match = true;
                                if (!id.isEmpty() && !String.valueOf(customer.getCustomerId()).contains(id))
                                        match = false;
                                if (!name.isEmpty() && !customer.getCustomerName().toLowerCase().contains(name))
                                        match = false;
                                if (!email.isEmpty() && !customer.getEmail().toLowerCase().contains(email))
                                        match = false;

                                if (match)
                                        tableModel.addRow(new Object[]{
                                                                                                               String.valueOf(customer.getCustomerId()),
                                                        customer.getCustomerName(),
                                                        customer.getEmail(),
                                                        customer.getAddress(), // <-- FIX: Add address in correct position
                                                        customer.getPhone(),
                                                        "✎"
                                        });
                        }
                }

                // Add this method to CustomerPanel
                private void showCustomerDetailDialog(int customerId) {
                    clearorder.model.Customer customer = customerDAO.getCustomerById(customerId);
                    if (customer == null) return;
                    javax.swing.JPanel panel = new javax.swing.JPanel(new java.awt.GridBagLayout());
                    java.awt.GridBagConstraints gbc = new java.awt.GridBagConstraints();
                    gbc.insets = new java.awt.Insets(5, 5, 5, 5);
                    gbc.gridx = 0; gbc.gridy = 0; gbc.anchor = java.awt.GridBagConstraints.WEST;
                    panel.add(new javax.swing.JLabel("Kundenname:"), gbc);
                    gbc.gridx = 1;
                    javax.swing.JTextField nameField = new javax.swing.JTextField(customer.getCustomerName(), 20);
                    panel.add(nameField, gbc);
                    gbc.gridx = 0; gbc.gridy++;
                    panel.add(new javax.swing.JLabel("E-Mail:"), gbc);
                    gbc.gridx = 1;
                    javax.swing.JTextField emailField = new javax.swing.JTextField(customer.getEmail(), 20);
                    panel.add(emailField, gbc);
                    gbc.gridx = 0; gbc.gridy++;
                    panel.add(new javax.swing.JLabel("Adresse:"), gbc);
                    gbc.gridx = 1;
                    javax.swing.JTextField addressField = new javax.swing.JTextField(customer.getAddress(), 20);
                    panel.add(addressField, gbc);
                    gbc.gridx = 0; gbc.gridy++;
                    panel.add(new javax.swing.JLabel("Telefonnummer:"), gbc);
                    gbc.gridx = 1;
                    javax.swing.JTextField phoneField = new javax.swing.JTextField(customer.getPhone(), 20);
                    panel.add(phoneField, gbc);
                    gbc.gridx = 0; gbc.gridy++;
                    gbc.gridwidth = 2;
                    panel.add(new javax.swing.JLabel("Unterschrift:"), gbc);
                    gbc.gridy++;
                    javax.swing.JTextPane signaturePane = new javax.swing.JTextPane();
                    signaturePane.setText(customer.getSignature() != null ? customer.getSignature() : "");
                    signaturePane.setFont(new java.awt.Font("Segoe Script", java.awt.Font.PLAIN, 18));
                    signaturePane.setPreferredSize(new Dimension(350, 60));
                    signaturePane.setMinimumSize(new Dimension(350, 60));
                    signaturePane.setMaximumSize(new Dimension(Short.MAX_VALUE, 60));
                    javax.swing.text.StyledDocument doc = signaturePane.getStyledDocument();
                    javax.swing.text.SimpleAttributeSet center = new javax.swing.text.SimpleAttributeSet();
                    javax.swing.text.StyleConstants.setAlignment(center, javax.swing.text.StyleConstants.ALIGN_CENTER);
                    doc.setParagraphAttributes(0, doc.getLength(), center, false);
                    javax.swing.JScrollPane sigScroll = new javax.swing.JScrollPane(signaturePane);
                    sigScroll.setPreferredSize(new Dimension(350, 60));
                    sigScroll.setMinimumSize(new Dimension(350, 60));
                    sigScroll.setMaximumSize(new Dimension(Short.MAX_VALUE, 60));
                    panel.add(sigScroll, gbc);
                    // Live font update
                    signaturePane.getDocument().addDocumentListener(new javax.swing.event.DocumentListener() {
                        public void insertUpdate(javax.swing.event.DocumentEvent e) { signaturePane.setFont(new java.awt.Font("Segoe Script", java.awt.Font.PLAIN, 18)); }
                        public void removeUpdate(javax.swing.event.DocumentEvent e) { signaturePane.setFont(new java.awt.Font("Segoe Script", java.awt.Font.PLAIN, 18)); }
                        public void changedUpdate(javax.swing.event.DocumentEvent e) { signaturePane.setFont(new java.awt.Font("Segoe Script", java.awt.Font.PLAIN, 18)); }
                    });
                    int result = javax.swing.JOptionPane.showConfirmDialog(this, panel, "Kundendetails bearbeiten", javax.swing.JOptionPane.OK_CANCEL_OPTION, javax.swing.JOptionPane.PLAIN_MESSAGE);
                    if (result == javax.swing.JOptionPane.OK_OPTION) {
                        customer.setCustomerName(nameField.getText().trim());
                        customer.setEmail(emailField.getText().trim());
                        customer.setAddress(addressField.getText().trim());
                        customer.setPhone(phoneField.getText().trim());
                        customer.setSignature(signaturePane.getText());
                        customerDAO.updateCustomer(customer);
                        loadCustomerData();
                    }
                }
        }

        // User List panel with enhanced styling
        static class UserListPanel extends JPanel {
                private JTable table;
                public DefaultTableModel tableModel;
                private JTextField userIdField, departmentField, nameField;
                private UserDAO userDAO;

                UserListPanel() {
                        this.userDAO = new UserDAO();
                        setLayout(new BorderLayout());
                        setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));
                        
                        // Create a combined north panel for header and search
                        JPanel northPanel = new JPanel(new BorderLayout());
                        
                        // Header panel
                        JPanel headerPanel = new JPanel(new BorderLayout());
                        JPanel titlePanel = new JPanel(new FlowLayout(FlowLayout.CENTER));
                        JLabel titleLabel = new JLabel("User List");
                        titleLabel.setFont(new Font("Arial", Font.BOLD, 20));
                        titlePanel.add(titleLabel);
                        headerPanel.add(titlePanel, BorderLayout.CENTER);
                        JButton addButton = new JButton("Create User");
                        addButton.setBackground(new Color(219, 68, 23));
                        addButton.setForeground(Color.WHITE);
                        addButton.setFocusPainted(false);
                        headerPanel.add(addButton, BorderLayout.EAST);
                        
                        // Search panel
                        JPanel searchPanel = new JPanel();
                        searchPanel.setLayout(new BoxLayout(searchPanel, BoxLayout.Y_AXIS));
                        searchPanel.setBorder(BorderFactory.createCompoundBorder(
                                        BorderFactory.createMatteBorder(0, 0, 1, 0, new Color(220, 220, 220)),
                                        BorderFactory.createEmptyBorder(10, 10, 10, 10))); // Reduced bottom padding from 20 to 10
                        JPanel filterPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 15, 5));
                        // User ID
                        JPanel userIdPanel = new JPanel(new BorderLayout(5, 0));
                        JLabel userIdLabel = new JLabel("User ID");
                        userIdLabel.setFont(new Font("Arial", Font.PLAIN, 12));
                        userIdField = new JTextField(15);
                        userIdField.setPreferredSize(new Dimension(userIdField.getPreferredSize().width, 30));
                        userIdPanel.add(userIdLabel, BorderLayout.NORTH);
                        userIdPanel.add(userIdField, BorderLayout.CENTER);
                        filterPanel.add(userIdPanel);
                        // Department
                        JPanel departmentPanel = new JPanel(new BorderLayout(5, 0));
                        JLabel departmentLabel = new JLabel("Department");
                        departmentLabel.setFont(new Font("Arial", Font.PLAIN, 12));
                        departmentField = new JTextField(15);
                        departmentField.setPreferredSize(new Dimension(departmentField.getPreferredSize().width, 30));
                        departmentPanel.add(departmentLabel, BorderLayout.NORTH);
                        departmentPanel.add(departmentField, BorderLayout.CENTER);
                        filterPanel.add(departmentPanel);
                        // Name
                        JPanel namePanel = new JPanel(new BorderLayout(5, 0));
                        JLabel nameLabel = new JLabel("Employee Name");
                        nameLabel.setFont(new Font("Arial", Font.PLAIN, 12));
                        nameField = new JTextField(15);
                        nameField.setPreferredSize(new Dimension(nameField.getPreferredSize().width, 30));
                        namePanel.add(nameLabel, BorderLayout.NORTH);
                        namePanel.add(nameField, BorderLayout.CENTER);
                        filterPanel.add(namePanel);
                        // Search button
                        JPanel searchButtonPanel = new JPanel(new BorderLayout());
                                               searchButtonPanel.setBorder(BorderFactory.createEmptyBorder(12, 0, 0, 0));
                        JButton searchButton = new JButton("Search");
                        searchButton.setBackground(new Color(51, 122, 220));
                        searchButton.setForeground(Color.WHITE);
                        searchButton.setFocusPainted(false);
                        searchButton.setPreferredSize(new Dimension(100, 30));
                        searchButtonPanel.add(searchButton, BorderLayout.CENTER);
                        filterPanel.add(searchButtonPanel);

                        searchPanel.add(filterPanel);
                        
                        // Add header and search to north panel
                        northPanel.add(headerPanel, BorderLayout.NORTH);
                        northPanel.add(searchPanel, BorderLayout.CENTER);
                        
                        // Add the combined north panel
                        add(northPanel, BorderLayout.NORTH);
                        // Table
                        String[] columns = { "User ID", "Employee Number", "Department", "E-Mail", "Employee Name",
                                        "Last Login Date", "Notes", "" };
                        tableModel = new DefaultTableModel(new Object[][]{}, columns) {
                                @Override
                                public boolean isCellEditable(int row, int column) {
                                        // Make only the last column editable (for the button)
                                        return column == 7;
                                }
                        };
                        table = new JTable(tableModel);
                        styleTable(table);
                        JScrollPane scrollPane = new JScrollPane(table);
                        scrollPane.setBorder(BorderFactory.createEmptyBorder());
                        // Add the table in the center so it takes up the remaining space
                        add(scrollPane, BorderLayout.CENTER);

                        loadUserData(); // Load data from DB

                        // Search action
                        searchButton.addActionListener(e -> {
                                filterUserData(); // Filter data based on search fields
                        });

                        // Add action listener for the Create User button
                        addButton.addActionListener(e -> {
            JFrame parentFrame = (JFrame) SwingUtilities.getWindowAncestor(UserListPanel.this);
            boolean userCreated = CreateUserDialog.showDialog(parentFrame);
            if (userCreated) {
                loadUserData(); // Refresh the user list
            }
        });
                }

                private void loadUserData() {
                        tableModel.setRowCount(0); // Clear existing data
                        List<clearorder.model.User> users = userDAO.getAllUsers();
                        for (clearorder.model.User user : users) {
                                tableModel.addRow(new Object[]{
                                        String.valueOf(user.getUserId()),
                                        user.getEmployeeNumber(),
                                        user.getDepartment(),
                                        user.getEmployeeEmail(),
                                        user.getEmployeeName(),
                                        user.getLastLoginDate(),
                                        user.getNotes(),
                                        ""  // Empty string for button column
                                });
                        }
                }

                private void filterUserData() {
                        String userId = userIdField.getText().trim();
                        String department = departmentField.getText().trim().toLowerCase();
                        String name = nameField.getText().trim().toLowerCase();

                        tableModel.setRowCount(0); // Clear existing data
                        List<clearorder.model.User> users = userDAO.getAllUsers(); // Re-fetch all data to filter

                        for (clearorder.model.User user : users) {
                                boolean match = true;
                                if (!userId.isEmpty() && !String.valueOf(user.getUserId()).contains(userId))
                                        match = false;
                                if (!department.isEmpty()
                                                && !user.getDepartment().toLowerCase().contains(department))
                                        match = false;
                                if (!name.isEmpty() && !user.getEmployeeName().toLowerCase().contains(name))
                                        match = false;
                                if (match)
                                        tableModel.addRow(new Object[]{
                                                        String.valueOf(user.getUserId()),
                                                        user.getEmployeeNumber(),
                                                        user.getDepartment(),
                                                        user.getEmployeeEmail(),
                                                        user.getEmployeeName(),
                                                        user.getLastLoginDate(),
                                                        user.getNotes(),
                                                        ""  // Empty string for button column
                                        });
                        }
                }
        }

        // CalendarDialog class
        static class CalendarDialog extends JDialog {
                private JCalendar calendar;
                private java.util.Date selectedDate;

                public CalendarDialog(Window parent) {
                        super(parent, "Tag auswählen", ModalityType.APPLICATION_MODAL);
                        setSize(300, 250);
                        setLocationRelativeTo(parent);
                        calendar = new JCalendar();
                        add(calendar, BorderLayout.CENTER);
                        JButton ok = new JButton("OK");
                        ok.addActionListener(e -> {
                                selectedDate = calendar.getDate();
                                dispose();
                        });
                        JPanel btnPanel = new JPanel();
                        btnPanel.add(ok);
                        add(btnPanel, BorderLayout.SOUTH);
                }

                public java.util.Date showDialog() {
                        setVisible(true);
                        return selectedDate;
                }
        }

        // JCalendar class
        static class JCalendar extends JPanel {
                private JSpinner spinner;

                public JCalendar() {
                        setLayout(new BorderLayout());
                        SpinnerDateModel model = new SpinnerDateModel();
                        spinner = new JSpinner(model);
                        JSpinner.DateEditor editor = new JSpinner.DateEditor(spinner, "dd.MM.yyyy");
                        spinner.setEditor(editor);
                        add(spinner, BorderLayout.CENTER);
                }

                public java.util.Date getDate() {
                        return (java.util.Date) spinner.getValue();
                }
        }

        // Button renderer for the table
        static class ButtonRenderer extends JButton implements TableCellRenderer {
        public ButtonRenderer() {
            setOpaque(true);
            setForeground(Color.WHITE);
            setBackground(new Color(51, 122, 220));
            setFocusPainted(false);
            setBorderPainted(true);
            setHorizontalAlignment(SwingConstants.CENTER);
        }

        @Override
        public Component getTableCellRendererComponent(JTable table, Object value,
                boolean isSelected, boolean hasFocus, int row, int column) {
            setText("Einsicht");
            return this;
        }
    }

    // Button editor for the table
    static class ButtonEditor extends AbstractCellEditor implements TableCellEditor {
        private JButton button;
        private String label;
        private int clickedRow;

        public ButtonEditor(JTable table) {
            button = new JButton();
            button.setOpaque(true);
            button.setForeground(Color.WHITE);
            button.setBackground(new Color(51, 122, 220));
            button.setFocusPainted(false);
            button.setBorderPainted(true);
            
            button.addActionListener(e -> {
                fireEditingStopped();
                
                // Get the selected user's ID
                int userId = Integer.parseInt(table.getValueAt(clickedRow, 0).toString());
                
                // Get the parent frame
                JFrame parentFrame = (JFrame) SwingUtilities.getWindowAncestor(table);
                
                // Get the user object from the database
                UserDAO userDAO = new UserDAO();
                User user = userDAO.getUserById(userId);
                
                if (user != null) {
                    // Show the edit dialog
                    boolean userUpdated = EditUserDialog.showDialog(parentFrame, user);
                    
                    // If the user was updated, refresh the table
                    if (userUpdated && table.getModel() instanceof DefaultTableModel) {
                        // Find the containing UserListPanel
                        Component comp = table;
                        while (comp != null && !(comp instanceof UserListPanel)) {
                            comp = comp.getParent();
                        }
                        
                        if (comp instanceof UserListPanel) {
                            ((UserListPanel) comp).loadUserData();
                        }
                    }
                }
            });
        }

        @Override
        public Component getTableCellEditorComponent(JTable table, Object value,
                boolean isSelected, int row, int column) {
            label = "Einsicht";
            button.setText(label);
            clickedRow = row;
            return button;
        }

        @Override
        public Object getCellEditorValue() {
            return label;
        }

        @Override
        public boolean stopCellEditing() {
            return super.stopCellEditing();
        }
    }
    
    // Add Customer Dialog with matching styling
    public static void showAddCustomerDialog(Component parent) {
        javax.swing.JPanel panel = new javax.swing.JPanel(new java.awt.GridBagLayout());
        java.awt.GridBagConstraints gbc = new java.awt.GridBagConstraints();
        gbc.insets = new java.awt.Insets(5, 5, 5, 5);
        gbc.gridx = 0; gbc.gridy = 0; gbc.anchor = java.awt.GridBagConstraints.WEST;

        panel.add(new javax.swing.JLabel("Kundenname:"), gbc);
        gbc.gridx = 1;
        javax.swing.JTextField nameField = new javax.swing.JTextField(20);
        panel.add(nameField, gbc);
        
        gbc.gridx = 0; gbc.gridy++;
        panel.add(new javax.swing.JLabel("E-Mail:"), gbc);
        gbc.gridx = 1;
        javax.swing.JTextField emailField = new javax.swing.JTextField(20);
        panel.add(emailField, gbc);
        
        gbc.gridx = 0; gbc.gridy++;
        panel.add(new javax.swing.JLabel("Adresse:"), gbc);
        gbc.gridx = 1;
        javax.swing.JTextField addressField = new javax.swing.JTextField(20);
        panel.add(addressField, gbc);
        
        gbc.gridx = 0; gbc.gridy++;
        panel.add(new javax.swing.JLabel("Telefonnummer:"), gbc);
        gbc.gridx = 1;
        javax.swing.JTextField phoneField = new javax.swing.JTextField(20);
        panel.add(phoneField, gbc);
        
        gbc.gridx = 0; gbc.gridy++;
        gbc.gridwidth = 2;
        panel.add(new javax.swing.JLabel("Unterschrift:"), gbc);
        gbc.gridy++;
        
        javax.swing.JTextPane signaturePane = new javax.swing.JTextPane();
        signaturePane.setFont(new java.awt.Font("Segoe Script", java.awt.Font.PLAIN, 18));
        signaturePane.setPreferredSize(new java.awt.Dimension(350, 60));
        signaturePane.setMinimumSize(new java.awt.Dimension(350, 60));
        signaturePane.setMaximumSize(new java.awt.Dimension(Short.MAX_VALUE, 60));
        
        javax.swing.text.StyledDocument doc = signaturePane.getStyledDocument();
        javax.swing.text.SimpleAttributeSet center = new javax.swing.text.SimpleAttributeSet();
        javax.swing.text.StyleConstants.setAlignment(center, javax.swing.text.StyleConstants.ALIGN_CENTER);
        doc.setParagraphAttributes(0, doc.getLength(), center, false);
        
        javax.swing.JScrollPane sigScroll = new javax.swing.JScrollPane(signaturePane);
        sigScroll.setPreferredSize(new java.awt.Dimension(350, 60));
        sigScroll.setMinimumSize(new java.awt.Dimension(350, 60));
        sigScroll.setMaximumSize(new java.awt.Dimension(Short.MAX_VALUE, 60));
        panel.add(sigScroll, gbc);
        
        // Live font update
        signaturePane.getDocument().addDocumentListener(new javax.swing.event.DocumentListener() {
            public void insertUpdate(javax.swing.event.DocumentEvent e) { signaturePane.setFont(new java.awt.Font("Segoe Script", java.awt.Font.PLAIN, 18)); }
            public void removeUpdate(javax.swing.event.DocumentEvent e) { signaturePane.setFont(new java.awt.Font("Segoe Script", java.awt.Font.PLAIN, 18)); }
            public void changedUpdate(javax.swing.event.DocumentEvent e) { signaturePane.setFont(new java.awt.Font("Segoe Script", java.awt.Font.PLAIN, 18)); }
        });
        
        // Find the MainFrame to center the dialog on the entire application
        java.awt.Frame mainFrame = javax.swing.JOptionPane.getFrameForComponent(parent);
        if (mainFrame == null) {
            // If we can't find the frame through the parent, try to get any frame
            java.awt.Frame[] frames = java.awt.Frame.getFrames();
            for (java.awt.Frame frame : frames) {
                if (frame.isVisible() && frame instanceof MainFrame) {
                    mainFrame = frame;
                    break;
                }
            }
        }
        
        int result = javax.swing.JOptionPane.showConfirmDialog(mainFrame, panel, "Kundendetails bearbeiten", 
                javax.swing.JOptionPane.OK_CANCEL_OPTION, javax.swing.JOptionPane.PLAIN_MESSAGE);
        
        if (result == javax.swing.JOptionPane.OK_OPTION) {
            String customerName = nameField.getText().trim();
            String customerEmail = emailField.getText().trim();
            String customerAddress = addressField.getText().trim();
            String customerPhone = phoneField.getText().trim();
            String signature = signaturePane.getText();
            
            if (!customerName.isEmpty() && !customerEmail.isEmpty() && !customerPhone.isEmpty() && !customerAddress.isEmpty()) {
                clearorder.dao.CustomerDAO customerDAO = new clearorder.dao.CustomerDAO();
                // Find max customer ID
                int nextCustomerId = 1;
                try {
                    java.util.List<clearorder.model.Customer> allCustomers = customerDAO.getAllCustomers();
                    for (clearorder.model.Customer c : allCustomers) {
                        if (c.getCustomerId() >= nextCustomerId) {
                            nextCustomerId = c.getCustomerId() + 1;
                        }
                    }
                } catch (Exception ex) {
                    ex.printStackTrace();
                }
                clearorder.model.Customer newCustomer = new clearorder.model.Customer(
                    nextCustomerId, customerName, customerEmail, customerPhone, customerAddress, signature);
                
                if (customerDAO.addCustomer(newCustomer)) {
                    javax.swing.JOptionPane.showMessageDialog(mainFrame, "Kunde wurde erfolgreich hinzugefügt.", "Kunde hinzugefügt", 
                            javax.swing.JOptionPane.INFORMATION_MESSAGE);
                    
                    // Refresh any customer panels
                    for (java.awt.Frame f : javax.swing.JFrame.getFrames()) {
                        if (f instanceof MainFrame) {
                            MainFrame main = (MainFrame) f;
                            for (java.awt.Component comp : main.mainPanel.getComponents()) {
                                if (comp instanceof MainFrame.CustomerPanel) {
                                    MainFrame.CustomerPanel customerPanel = (MainFrame.CustomerPanel) comp;
                                    customerPanel.loadCustomerData();
                                    break;
                                }
                            }
                        }
                    }
                } else {
                    javax.swing.JOptionPane.showMessageDialog(mainFrame, "Kunde konnte nicht hinzugefügt werden.", "Fehler", 
                            javax.swing.JOptionPane.ERROR_MESSAGE);
                }
            } else {
                javax.swing.JOptionPane.showMessageDialog(mainFrame, "Alle Felder müssen ausgefüllt werden.", 
                        "Fehlende Informationen", javax.swing.JOptionPane.WARNING_MESSAGE);
            }
        }
    }
}