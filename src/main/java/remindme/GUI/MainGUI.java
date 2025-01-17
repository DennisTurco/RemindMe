package remindme.GUI;

import java.awt.Dimension;
import java.awt.Image;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.KeyEvent;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import javax.swing.AbstractAction;
import javax.swing.ActionMap;
import javax.swing.ImageIcon;
import javax.swing.InputMap;
import javax.swing.JComponent;
import javax.swing.JOptionPane;
import javax.swing.JScrollPane;
import javax.swing.KeyStroke;
import javax.swing.SwingUtilities;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableColumnModel;

import com.formdev.flatlaf.FlatClientProperties;

import remindme.Logger;
import remindme.Entities.Remind;
import remindme.Enums.ConfigKey;
import remindme.Enums.MenuItems;
import remindme.Enums.TranslationLoaderEnum.TranslationCategory;
import remindme.Enums.TranslationLoaderEnum.TranslationKey;
import remindme.Json.JSONConfigReader;
import remindme.Managers.ThemeManager;
import remindme.Table.CheckboxCellRenderer;
import remindme.Table.RemindTable;
import remindme.Table.RemindTableModel;
import remindme.Table.StripedRowRenderer;
import remindme.Managers.RemindManager;

/**
 * @author Dennis Turco
 */
public final class MainGUI extends javax.swing.JFrame {
    private static final JSONConfigReader configReader = new JSONConfigReader(ConfigKey.CONFIG_FILE_STRING.getValue(), ConfigKey.CONFIG_DIRECTORY_STRING.getValue());
    
    
    private Integer selectedRow;
    private final RemindManager remindManager;

    public static DefaultTableModel model;
    public static RemindTable remindTable;
    public static RemindTableModel tableModel;
    
    public MainGUI() {
        ThemeManager.updateThemeFrame(this);
        
        initComponents();

        remindManager = new RemindManager(this);

        // logo application
        Image icon = new ImageIcon(this.getClass().getResource(ConfigKey.LOGO_IMG.getValue())).getImage();
        this.setIconImage(icon);

        // load Menu items
        setMenuItems();
        
        // set app sizes
        setScreenSize();

        initializeTable();

        // icons
        researchField.putClientProperty(FlatClientProperties.TEXT_FIELD_LEADING_ICON, new com.formdev.flatlaf.extras.FlatSVGIcon("res/img/search.svg", 16, 16));

        // translations
        setTranslations();
        
        // set all svg images
        setSvgImages();
    }
    
    public void showWindow() {
        setVisible(true);
        toFront();
        requestFocus();
    }

    private void setScreenSize() {
        Dimension size = Toolkit.getDefaultToolkit().getScreenSize();
        int width = Math.min((int) size.getWidth(), Integer.parseInt(ConfigKey.GUI_WIDTH.getValue()));
        int height = Math.min((int) size.getHeight(), Integer.parseInt(ConfigKey.GUI_HEIGHT.getValue()));

        this.setSize(width,height);
    }

    public void initializeTable() {
        List<Remind> reminds = remindManager.retriveAndGetReminds();
        displayRemindList(reminds);
    }

    private void displayRemindList(List<Remind> reminds) {
        RemindTableModel tempModel = new RemindTableModel(remindManager.getColumnTranslations(), 0);
    
        // Populate the model with remind data
        for (Remind remind : reminds) {
            tempModel.addRow(new Object[]{
                remind.getName(),
                    remind.isActive(),
                    remind.isTopLevel(),
                    remind.getLastExecution() != null ? remind.getLastExecution().format(RemindManager.formatter) : "",
                    remind.getNextExecution() != null ? remind.getNextExecution().format(RemindManager.formatter) : "",
                    remind.getTimeInterval() != null ? remind.getTimeInterval().toString() : ""
            });
        }
    
        remindTable = new RemindTable(tempModel);

        // Add key bindings using InputMap and ActionMap
        InputMap inputMap = remindTable.getInputMap(JComponent.WHEN_ANCESTOR_OF_FOCUSED_COMPONENT);
        ActionMap actionMap = remindTable.getActionMap();

        // Handle Enter key
        inputMap.put(KeyStroke.getKeyStroke(KeyEvent.VK_ENTER, 0), "enterKey");
        actionMap.put("enterKey", new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent e) {
                int selectedRow = remindTable.getSelectedRow();
                if (selectedRow == -1) return;

                Logger.logMessage("Enter key pressed on row: " + selectedRow, Logger.LogLevel.DEBUG);
                //TODO: OpenRemind((String) remindTable.getValueAt(selectedRow, 0));
            }
        });

        // Handle Delete key
        inputMap.put(KeyStroke.getKeyStroke(KeyEvent.VK_DELETE, 0), "deleteKey");
        actionMap.put("deleteKey", new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent e) {
                int[] selectedRows = remindTable.getSelectedRows();
                if (selectedRows.length == 0) return;
        
                Logger.logMessage("Delete key pressed on rows: " + Arrays.toString(selectedRows), Logger.LogLevel.DEBUG);

                int response = JOptionPane.showConfirmDialog(null, TranslationCategory.DIALOGS.getTranslation(TranslationKey.CONFIRMATION_DELETION_MESSAGE), TranslationCategory.DIALOGS.getTranslation(TranslationKey.CONFIRMATION_DELETION_TITLE), JOptionPane.YES_NO_OPTION, JOptionPane.QUESTION_MESSAGE);
                if (response != JOptionPane.YES_OPTION) {
                    return;
                }

                for (int row : selectedRows) {
                    remindManager.removeReminder(row, false);
                }
            }
        });

        // Apply renderers for each column
        TableColumnModel columnModel = remindTable.getColumnModel();

        for (int i = 0; i < columnModel.getColumnCount(); i++) {
            if (i == 1 || i == 2) {
                columnModel.getColumn(i).setCellRenderer(new CheckboxCellRenderer());
                columnModel.getColumn(i).setCellEditor(remindTable.getDefaultEditor(Boolean.class));
            } else {
                columnModel.getColumn(i).setCellRenderer(new StripedRowRenderer());
            }
        }
            
        // Add the existing mouse listener to the new table
        remindTable.addMouseListener(new java.awt.event.MouseAdapter() {
            @Override
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                tableMouseClicked(evt); // Reuse the existing method
            }
        });
    
        // Update the global model reference
        MainGUI.model = tempModel;
    
        // Replace the existing table in the GUI
        JScrollPane scrollPane = (JScrollPane) table.getParent().getParent();
        table = remindTable; // Update the reference to the new table
        scrollPane.setViewportView(table); // Replace the table in the scroll pane
    }

    public void setMenuItems() {
        MenuBugReport.setVisible(configReader.isMenuItemEnabled(MenuItems.BugReport.name()));
        MenuPreferences.setVisible(configReader.isMenuItemEnabled(MenuItems.Preferences.name()));
        MenuDonate.setVisible(configReader.isMenuItemEnabled(MenuItems.Donate.name()));
        MenuHistory.setVisible(configReader.isMenuItemEnabled(MenuItems.History.name()));
        MenuInfoPage.setVisible(configReader.isMenuItemEnabled(MenuItems.InfoPage.name()));
        MenuNew.setVisible(configReader.isMenuItemEnabled(MenuItems.New.name()));
        MenuQuit.setVisible(configReader.isMenuItemEnabled(MenuItems.Quit.name()));
        MenuShare.setVisible(configReader.isMenuItemEnabled(MenuItems.Share.name()));
        MenuSupport.setVisible(configReader.isMenuItemEnabled(MenuItems.Support.name()));
        MenuWebsite.setVisible(configReader.isMenuItemEnabled(MenuItems.Website.name()));
        MenuImport.setVisible(configReader.isMenuItemEnabled(MenuItems.Import.name()));
        MenuExport.setVisible(configReader.isMenuItemEnabled(MenuItems.Export.name()));
    }

    public void setTranslations() {
        // general
        versionLabel.setText(TranslationCategory.GENERAL.getTranslation(TranslationKey.VERSION) + " " + ConfigKey.VERSION.getValue());
        
        // menu
        jMenu1.setText(TranslationCategory.MENU.getTranslation(TranslationKey.FILE));
        jMenu2.setText(TranslationCategory.MENU.getTranslation(TranslationKey.OPTIONS));
        jMenu3.setText(TranslationCategory.MENU.getTranslation(TranslationKey.ABOUT));
        jMenu5.setText(TranslationCategory.MENU.getTranslation(TranslationKey.HELP));

        // menu items
        MenuBugReport.setText(TranslationCategory.MENU.getTranslation(TranslationKey.BUG_REPORT));
        MenuDonate.setText(TranslationCategory.MENU.getTranslation(TranslationKey.DONATE));
        MenuHistory.setText(TranslationCategory.MENU.getTranslation(TranslationKey.HISTORY));
        MenuInfoPage.setText(TranslationCategory.MENU.getTranslation(TranslationKey.INFO_PAGE));
        MenuNew.setText(TranslationCategory.MENU.getTranslation(TranslationKey.NEW));
        MenuQuit.setText(TranslationCategory.MENU.getTranslation(TranslationKey.QUIT));
        MenuPreferences.setText(TranslationCategory.MENU.getTranslation(TranslationKey.PREFERENCES));
        MenuImport.setText(TranslationCategory.MENU.getTranslation(TranslationKey.IMPORT));
        MenuExport.setText(TranslationCategory.MENU.getTranslation(TranslationKey.EXPORT));
        MenuShare.setText(TranslationCategory.MENU.getTranslation(TranslationKey.SHARE));
        MenuSupport.setText(TranslationCategory.MENU.getTranslation(TranslationKey.SUPPORT));
        MenuWebsite.setText(TranslationCategory.MENU.getTranslation(TranslationKey.WEBSITE));

        // remind list
        ExportLabel.setText(TranslationCategory.MAIN_FRAME.getTranslation(TranslationKey.EXPORT_AS));
        newRemindBtn.setToolTipText(TranslationCategory.MAIN_FRAME.getTranslation(TranslationKey.NEW_REMIND_TOOLTIP));
        exportAsPdfBtn.setToolTipText(TranslationCategory.MAIN_FRAME.getTranslation(TranslationKey.EXPORT_AS_PDF_TOOLTIP));
        exportAsCsvBtn.setToolTipText(TranslationCategory.MAIN_FRAME.getTranslation(TranslationKey.EXPORT_AS_CSV_TOOLTIP));
        researchField.setToolTipText(TranslationCategory.MAIN_FRAME.getTranslation(TranslationKey.RESEARCH_BAR_TOOLTIP));
        researchField.putClientProperty(FlatClientProperties.PLACEHOLDER_TEXT, TranslationCategory.MAIN_FRAME.getTranslation(TranslationKey.RESEARCH_BAR_PLACEHOLDER));

        // popup
        copyRemindNamePopupItem.setText(TranslationCategory.REMIND_LIST.getTranslation(TranslationKey.COPY_NAME_POPUP));
        DeletePopupItem.setText(TranslationCategory.REMIND_LIST.getTranslation(TranslationKey.DELETE_POPUP));
        DuplicatePopupItem.setText(TranslationCategory.REMIND_LIST.getTranslation(TranslationKey.DUPLICATE_POPUP));
        EditPoputItem.setText(TranslationCategory.REMIND_LIST.getTranslation(TranslationKey.EDIT_POPUP));
        renamePopupItem.setText(TranslationCategory.REMIND_LIST.getTranslation(TranslationKey.RENAME_POPUP));
    }
    
    public void setSvgImages() {
        exportAsCsvBtn.setSvgImage("res/img/csv.svg", 30, 30);
        exportAsPdfBtn.setSvgImage("res/img/pdf.svg", 30, 30);
        newRemindBtn.setSvgImage("res/img/add.svg", 30, 30);
        MenuImport.setSvgImage("res/img/import.svg", 16, 16);
        MenuExport.setSvgImage("res/img/export.svg", 16, 16);
        MenuNew.setSvgImage("res/img/new_file.svg", 16, 16);
        MenuBugReport.setSvgImage("res/img/bug.svg", 16, 16);
        MenuHistory.setSvgImage("res/img/history.svg", 16, 16);
        MenuDonate.setSvgImage("res/img/donate.svg", 16, 16);
        MenuPreferences.setSvgImage("res/img/settings.svg", 16, 16);
        MenuShare.setSvgImage("res/img/share.svg", 16, 16);
        MenuSupport.setSvgImage("res/img/support.svg", 16, 16);
        MenuWebsite.setSvgImage("res/img/website.svg", 16, 16);
        MenuQuit.setSvgImage("res/img/quit.svg", 16, 16);
        MenuInfoPage.setSvgImage("res/img/info.svg", 16, 16);
    }

    /**
     * This method is called from within the constructor to initialize the form.
     *  
     * WARNING: Do NOT modify this code. The content of this method is always
     * regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        TablePopup = new javax.swing.JPopupMenu();
        EditPoputItem = new javax.swing.JMenuItem();
        DeletePopupItem = new javax.swing.JMenuItem();
        DuplicatePopupItem = new javax.swing.JMenuItem();
        renamePopupItem = new javax.swing.JMenuItem();
        jMenu4 = new javax.swing.JMenu();
        copyRemindNamePopupItem = new javax.swing.JMenuItem();
        jPanel2 = new javax.swing.JPanel();
        tablePanel = new javax.swing.JPanel();
        jScrollPane1 = new javax.swing.JScrollPane();
        table = new javax.swing.JTable();
        jLabel1 = new javax.swing.JLabel();
        researchField = new javax.swing.JTextField();
        ExportLabel = new javax.swing.JLabel();
        exportAsCsvBtn = new remindme.Svg.SVGButton();
        exportAsPdfBtn = new remindme.Svg.SVGButton();
        newRemindBtn = new remindme.Svg.SVGButton();
        detailsPanel = new javax.swing.JPanel();
        detailsLabel = new javax.swing.JLabel();
        versionLabel = new javax.swing.JLabel();
        jMenuBar1 = new javax.swing.JMenuBar();
        jMenu1 = new javax.swing.JMenu();
        MenuNew = new remindme.Svg.SVGMenuItem();
        jSeparator4 = new javax.swing.JPopupMenu.Separator();
        MenuImport = new remindme.Svg.SVGMenuItem();
        MenuExport = new remindme.Svg.SVGMenuItem();
        jSeparator5 = new javax.swing.JPopupMenu.Separator();
        MenuHistory = new remindme.Svg.SVGMenuItem();
        jMenu2 = new javax.swing.JMenu();
        MenuPreferences = new remindme.Svg.SVGMenuItem();
        MenuQuit = new remindme.Svg.SVGMenuItem();
        jMenu3 = new javax.swing.JMenu();
        MenuWebsite = new remindme.Svg.SVGMenuItem();
        MenuInfoPage = new remindme.Svg.SVGMenuItem();
        MenuShare = new remindme.Svg.SVGMenuItem();
        MenuDonate = new remindme.Svg.SVGMenuItem();
        jMenu5 = new javax.swing.JMenu();
        MenuBugReport = new remindme.Svg.SVGMenuItem();
        MenuSupport = new remindme.Svg.SVGMenuItem();

        EditPoputItem.setText("Edit");
        TablePopup.add(EditPoputItem);

        DeletePopupItem.setText("Delete");
        TablePopup.add(DeletePopupItem);

        DuplicatePopupItem.setText("Duplicate");
        TablePopup.add(DuplicatePopupItem);

        renamePopupItem.setText("Rename remind");
        TablePopup.add(renamePopupItem);

        jMenu4.setText("Copy text");

        copyRemindNamePopupItem.setText("Copy remind name");
        jMenu4.add(copyRemindNamePopupItem);

        TablePopup.add(jMenu4);

        setDefaultCloseOperation(javax.swing.WindowConstants.DISPOSE_ON_CLOSE);
        setTitle("Remind Me");
        setResizable(false);

        tablePanel.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                tablePanelMouseClicked(evt);
            }
        });

        table.setModel(new javax.swing.table.DefaultTableModel(
            new Object [][] {

            },
            new String [] {
                "Name", "Active", "Top Level", "Last Execution", "Next Execution", "Time interval"
            }
        ) {
            Class[] types = new Class [] {
                java.lang.String.class, java.lang.Boolean.class, java.lang.Boolean.class, java.lang.String.class, java.lang.String.class, java.lang.String.class
            };
            boolean[] canEdit = new boolean [] {
                false, false, false, false, false, false
            };

            public Class getColumnClass(int columnIndex) {
                return types [columnIndex];
            }

            public boolean isCellEditable(int rowIndex, int columnIndex) {
                return canEdit [columnIndex];
            }
        });
        table.setCursor(new java.awt.Cursor(java.awt.Cursor.CROSSHAIR_CURSOR));
        table.setRowHeight(50);
        table.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                tableMouseClicked(evt);
            }
        });
        jScrollPane1.setViewportView(table);

        jLabel1.setFont(new java.awt.Font("Segoe UI", 0, 20)); // NOI18N
        jLabel1.setText("|");
        jLabel1.setAlignmentY(0.0F);

        researchField.setToolTipText("Research bar");
        researchField.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyTyped(java.awt.event.KeyEvent evt) {
                researchFieldKeyTyped(evt);
            }
        });

        ExportLabel.setHorizontalAlignment(javax.swing.SwingConstants.RIGHT);
        ExportLabel.setText("Export As:");

        exportAsCsvBtn.setToolTipText("Export as .csv");
        exportAsCsvBtn.setMaximumSize(new java.awt.Dimension(32, 32));
        exportAsCsvBtn.setMinimumSize(new java.awt.Dimension(32, 32));
        exportAsCsvBtn.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                exportAsCsvBtnActionPerformed(evt);
            }
        });

        exportAsPdfBtn.setToolTipText("Export as .pdf");
        exportAsPdfBtn.setMaximumSize(new java.awt.Dimension(32, 32));
        exportAsPdfBtn.setMinimumSize(new java.awt.Dimension(32, 32));
        exportAsPdfBtn.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                exportAsPdfBtnActionPerformed(evt);
            }
        });

        newRemindBtn.setToolTipText("Add new reminder");
        newRemindBtn.setMaximumSize(new java.awt.Dimension(32, 32));
        newRemindBtn.setMinimumSize(new java.awt.Dimension(32, 32));
        newRemindBtn.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                addBackupEntryButtonActionPerformed(evt);
            }
        });

        javax.swing.GroupLayout tablePanelLayout = new javax.swing.GroupLayout(tablePanel);
        tablePanel.setLayout(tablePanelLayout);
        tablePanelLayout.setHorizontalGroup(
            tablePanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(tablePanelLayout.createSequentialGroup()
                .addContainerGap()
                .addGroup(tablePanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jScrollPane1, javax.swing.GroupLayout.DEFAULT_SIZE, 950, Short.MAX_VALUE)
                    .addGroup(tablePanelLayout.createSequentialGroup()
                        .addComponent(newRemindBtn, javax.swing.GroupLayout.PREFERRED_SIZE, 32, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addGap(9, 9, 9)
                        .addComponent(jLabel1, javax.swing.GroupLayout.PREFERRED_SIZE, 9, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(researchField, javax.swing.GroupLayout.PREFERRED_SIZE, 321, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                        .addComponent(ExportLabel, javax.swing.GroupLayout.PREFERRED_SIZE, 238, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(exportAsCsvBtn, javax.swing.GroupLayout.PREFERRED_SIZE, 32, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(exportAsPdfBtn, javax.swing.GroupLayout.PREFERRED_SIZE, 32, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addGap(1, 1, 1)))
                .addContainerGap())
        );
        tablePanelLayout.setVerticalGroup(
            tablePanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(tablePanelLayout.createSequentialGroup()
                .addContainerGap()
                .addGroup(tablePanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(tablePanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                        .addComponent(exportAsCsvBtn, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.PREFERRED_SIZE, 32, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addGroup(tablePanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                            .addComponent(jLabel1)
                            .addComponent(researchField, javax.swing.GroupLayout.PREFERRED_SIZE, 34, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(ExportLabel)))
                    .addComponent(exportAsPdfBtn, javax.swing.GroupLayout.PREFERRED_SIZE, 32, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(newRemindBtn, javax.swing.GroupLayout.PREFERRED_SIZE, 32, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jScrollPane1, javax.swing.GroupLayout.DEFAULT_SIZE, 485, Short.MAX_VALUE)
                .addContainerGap())
        );

        researchField.getAccessibleContext().setAccessibleName("");

        detailsLabel.setVerticalAlignment(javax.swing.SwingConstants.TOP);

        javax.swing.GroupLayout detailsPanelLayout = new javax.swing.GroupLayout(detailsPanel);
        detailsPanel.setLayout(detailsPanelLayout);
        detailsPanelLayout.setHorizontalGroup(
            detailsPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, detailsPanelLayout.createSequentialGroup()
                .addContainerGap()
                .addComponent(detailsLabel, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addContainerGap())
        );
        detailsPanelLayout.setVerticalGroup(
            detailsPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(detailsPanelLayout.createSequentialGroup()
                .addComponent(detailsLabel, javax.swing.GroupLayout.PREFERRED_SIZE, 78, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );

        javax.swing.GroupLayout jPanel2Layout = new javax.swing.GroupLayout(jPanel2);
        jPanel2.setLayout(jPanel2Layout);
        jPanel2Layout.setHorizontalGroup(
            jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel2Layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(detailsPanel, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(tablePanel, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)))
        );
        jPanel2Layout.setVerticalGroup(
            jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, jPanel2Layout.createSequentialGroup()
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addComponent(tablePanel, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(detailsPanel, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap())
        );

        versionLabel.setText("Version 2.0.2");

        jMenu1.setText("File");

        MenuNew.setAccelerator(javax.swing.KeyStroke.getKeyStroke(java.awt.event.KeyEvent.VK_N, java.awt.event.InputEvent.CTRL_DOWN_MASK));
        MenuNew.setText("New");
        MenuNew.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                MenuNewActionPerformed(evt);
            }
        });
        jMenu1.add(MenuNew);
        jMenu1.add(jSeparator4);

        MenuImport.setText("Import remind list");
        jMenu1.add(MenuImport);

        MenuExport.setText("Export remind list");
        jMenu1.add(MenuExport);
        jMenu1.add(jSeparator5);

        MenuHistory.setText("History");
        jMenu1.add(MenuHistory);

        jMenuBar1.add(jMenu1);

        jMenu2.setText("Options");

        MenuPreferences.setText("Preferences");
        MenuPreferences.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                MenuPreferencesActionPerformed(evt);
            }
        });
        jMenu2.add(MenuPreferences);

        MenuQuit.setAccelerator(javax.swing.KeyStroke.getKeyStroke(java.awt.event.KeyEvent.VK_F4, java.awt.event.InputEvent.ALT_DOWN_MASK));
        MenuQuit.setText("Quit");
        jMenu2.add(MenuQuit);

        jMenuBar1.add(jMenu2);

        jMenu3.setText("About");

        MenuWebsite.setText("Website");
        jMenu3.add(MenuWebsite);

        MenuInfoPage.setText("Info");
        jMenu3.add(MenuInfoPage);

        MenuShare.setText("Share");
        jMenu3.add(MenuShare);

        MenuDonate.setText("Donate");
        jMenu3.add(MenuDonate);

        jMenuBar1.add(jMenu3);

        jMenu5.setText("Help");

        MenuBugReport.setText("Report a bug");
        jMenu5.add(MenuBugReport);

        MenuSupport.setText("Support");
        jMenu5.add(MenuSupport);

        jMenuBar1.add(jMenu5);

        setJMenuBar(jMenuBar1);

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(getContentPane());
        getContentPane().setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(versionLabel, javax.swing.GroupLayout.DEFAULT_SIZE, 956, Short.MAX_VALUE)
                .addContainerGap())
            .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                .addGroup(layout.createSequentialGroup()
                    .addGap(0, 0, Short.MAX_VALUE)
                    .addComponent(jPanel2, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addGap(0, 0, Short.MAX_VALUE)))
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addGap(654, 654, 654)
                .addComponent(versionLabel)
                .addContainerGap(7, Short.MAX_VALUE))
            .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                .addGroup(layout.createSequentialGroup()
                    .addGap(0, 0, Short.MAX_VALUE)
                    .addComponent(jPanel2, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addGap(0, 0, Short.MAX_VALUE)))
        );

        pack();
        setLocationRelativeTo(null);
    }// </editor-fold>//GEN-END:initComponents
    
    private void MenuPreferencesActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_MenuPreferencesActionPerformed
        remindManager.openPreferences();
    }//GEN-LAST:event_MenuPreferencesActionPerformed

    private void tablePanelMouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_tablePanelMouseClicked
        table.clearSelection(); // deselect any selected row
        detailsLabel.setText(""); // clear the label
    }//GEN-LAST:event_tablePanelMouseClicked

    private void addBackupEntryButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_addBackupEntryButtonActionPerformed
        // TODO add your handling code here:
    }//GEN-LAST:event_addBackupEntryButtonActionPerformed

    private void exportAsPdfBtnActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_exportAsPdfBtnActionPerformed
        // TODO add your handling code here:
    }//GEN-LAST:event_exportAsPdfBtnActionPerformed

    private void exportAsCsvBtnActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_exportAsCsvBtnActionPerformed
        // TODO add your handling code here:
    }//GEN-LAST:event_exportAsCsvBtnActionPerformed

    private void researchFieldKeyTyped(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_researchFieldKeyTyped
        // TODO add your handling code here:
    }//GEN-LAST:event_researchFieldKeyTyped

    private void tableMouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_tableMouseClicked
        selectedRow = table.rowAtPoint(evt.getPoint()); // get index of the row

        if (selectedRow == -1) { // if clicked outside valid rows
            table.clearSelection(); // deselect any selected row
            detailsLabel.setText(""); // clear the label
        } else {
            // get correct remind
            String remindName = (String) remindTable.getValueAt(selectedRow, 0);
            Remind remind = Remind.getRemindByName(new ArrayList<>(remindManager.getReminds()), remindName);

            Logger.logMessage("Selected remind: " + remindName, Logger.LogLevel.DEBUG);

            // Handling right mouse button click
            if (SwingUtilities.isRightMouseButton(evt)) {
                Logger.logMessage("Right click on row: " + selectedRow, Logger.LogLevel.INFO);
                table.setRowSelectionInterval(selectedRow, selectedRow); // select clicked row
                TablePopup.show(evt.getComponent(), evt.getX(), evt.getY()); // show popup
            }

            // Handling left mouse button double-click
            else if (SwingUtilities.isLeftMouseButton(evt) && evt.getClickCount() == 2) {
                Logger.logMessage("Double-click on row: " + selectedRow, Logger.LogLevel.INFO);
                //TODO: OpenBackup(remindName);
            }

            // Handling single left mouse button click
            else if (SwingUtilities.isLeftMouseButton(evt)) {
                String remindNameStr = TranslationCategory.REMIND_LIST.getTranslation(TranslationKey.NAME_DETAIL);
                String descriptionStr = TranslationCategory.REMIND_LIST.getTranslation(TranslationKey.DESCRIPTION_DETAIL);
                String isActiveStr = TranslationCategory.REMIND_LIST.getTranslation(TranslationKey.IS_ACTIVE_DETAIL);
                String isTopLevelStr = TranslationCategory.REMIND_LIST.getTranslation(TranslationKey.IS_TOP_LEVEL_DETAIL);
                String lastExeutionStr = TranslationCategory.REMIND_LIST.getTranslation(TranslationKey.LAST_EXECUTION_DETAIL);
                String nextExecutionStr = TranslationCategory.REMIND_LIST.getTranslation(TranslationKey.NEXT_EXECUTION_DETAIL);
                String timeIntervalStr = TranslationCategory.REMIND_LIST.getTranslation(TranslationKey.TIME_INTERVAL_DETAIL);
                String creationDateStr = TranslationCategory.REMIND_LIST.getTranslation(TranslationKey.CREATION_DATE_DETAIL);
                String lastUpdateDateStr = TranslationCategory.REMIND_LIST.getTranslation(TranslationKey.LAST_UPDATE_DATE_DETAIL);
                String remindCountStr = TranslationCategory.REMIND_LIST.getTranslation(TranslationKey.COUNT_DETAIL);

                detailsLabel.setText(
                    "<html><b>" + remindNameStr + ":</b> " + remind.getName() + ", " +
                    "<b>" + descriptionStr + ":</b> " + remind.getDescription() + ", " +
                    "<b>" + isActiveStr + ":</b> " + remind.isActive() + ", " +
                    "<b>" + isTopLevelStr + ":</b> " + remind.isTopLevel() + ", " +
                    "<b>" + lastExeutionStr + ":</b> " + (remind.getLastExecution() != null ? remind.getLastExecution().format(RemindManager.formatter) : "") + ", " +
                    "<b>" + nextExecutionStr + ":</b> " + (remind.getNextExecution() != null ? remind.getNextExecution().format(RemindManager.formatter) : "_") + ", " +
                    "<b>" + timeIntervalStr + ":</b> " + (remind.getTimeInterval() != null ? remind.getTimeInterval().toString() : "_") + ", " +
                    "<b>" + creationDateStr + ":</b> " + (remind.getCreationDate() != null ? remind.getCreationDate().format(RemindManager.formatter) : "_") + ", " +
                    "<b>" + lastUpdateDateStr + ":</b> " + (remind.getLastUpdateDate() != null ? remind.getLastUpdateDate().format(RemindManager.formatter) : "_") + ", " +
                    "<b>" + remindCountStr + ":</b> " + (remind.getRemindCount()) + ", " +
                    "</html>"
                );
            }
        }
    }//GEN-LAST:event_tableMouseClicked

    private void MenuNewActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_MenuNewActionPerformed
        // TODO add your handling code here:
    }//GEN-LAST:event_MenuNewActionPerformed

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JMenuItem copyRemindNamePopupItem;
    private javax.swing.JMenuItem DeletePopupItem;
    private javax.swing.JMenuItem DuplicatePopupItem;
    private javax.swing.JMenuItem EditPoputItem;
    private javax.swing.JLabel ExportLabel;
    private remindme.Svg.SVGMenuItem MenuBugReport;
    private remindme.Svg.SVGMenuItem MenuDonate;
    private remindme.Svg.SVGMenuItem MenuExport;
    private remindme.Svg.SVGMenuItem MenuHistory;
    private remindme.Svg.SVGMenuItem MenuImport;
    private remindme.Svg.SVGMenuItem MenuInfoPage;
    private remindme.Svg.SVGMenuItem MenuNew;
    private remindme.Svg.SVGMenuItem MenuPreferences;
    private remindme.Svg.SVGMenuItem MenuQuit;
    private remindme.Svg.SVGMenuItem MenuShare;
    private remindme.Svg.SVGMenuItem MenuSupport;
    private remindme.Svg.SVGMenuItem MenuWebsite;
    private javax.swing.JPopupMenu TablePopup;
    private remindme.Svg.SVGButton newRemindBtn;
    private javax.swing.JLabel detailsLabel;
    private javax.swing.JPanel detailsPanel;
    private remindme.Svg.SVGButton exportAsCsvBtn;
    private remindme.Svg.SVGButton exportAsPdfBtn;
    private javax.swing.JLabel jLabel1;
    private javax.swing.JMenu jMenu1;
    private javax.swing.JMenu jMenu2;
    private javax.swing.JMenu jMenu3;
    private javax.swing.JMenu jMenu4;
    private javax.swing.JMenu jMenu5;
    private javax.swing.JMenuBar jMenuBar1;
    private javax.swing.JPanel jPanel2;
    private javax.swing.JScrollPane jScrollPane1;
    private javax.swing.JPopupMenu.Separator jSeparator4;
    private javax.swing.JPopupMenu.Separator jSeparator5;
    private javax.swing.JMenuItem renamePopupItem;
    private javax.swing.JTextField researchField;
    private javax.swing.JTable table;
    private javax.swing.JPanel tablePanel;
    private javax.swing.JLabel versionLabel;
    // End of variables declaration//GEN-END:variables
}