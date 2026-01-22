package remindme.GUI;

import java.awt.Dimension;
import java.awt.Image;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.KeyEvent;
import java.awt.event.MouseAdapter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;

import javax.swing.AbstractAction;
import javax.swing.ActionMap;
import javax.swing.ImageIcon;
import javax.swing.InputMap;
import javax.swing.JComponent;
import javax.swing.JOptionPane;
import javax.swing.JPopupMenu;
import javax.swing.JTable;
import javax.swing.KeyStroke;
import javax.swing.SwingUtilities;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableColumnModel;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.formdev.flatlaf.FlatClientProperties;

import remindme.Dialogs.EntryUserDialog;
import remindme.Email.EmailSender;
import remindme.Entities.Preferences;
import remindme.Entities.Remind;
import remindme.Entities.User;
import remindme.Enums.ConfigKey;
import remindme.Enums.ExecutionMethod;
import remindme.Enums.LanguagesEnum;
import remindme.Enums.MenuItems;
import remindme.Enums.TranslationLoaderEnum.TranslationCategory;
import remindme.Enums.TranslationLoaderEnum.TranslationKey;
import remindme.GUI.Controllers.MainController;
import remindme.GUI.Controllers.MainImportExportController;
import remindme.GUI.Controllers.MainItemController;
import remindme.GUI.Controllers.MainPopupController;
import remindme.Json.JSONConfigReader;
import remindme.Json.JsonUser;
import remindme.Managers.ThemeManager;
import remindme.Services.RemindService;
import remindme.Table.CheckboxCellRenderer;
import remindme.Table.RemindTable;
import remindme.Table.RemindTableModel;
import remindme.Table.StripedRowRenderer;
import remindme.Table.SvgImageRenderer;
import remindme.Table.TableDataManager;

/**
 * @author Dennis Turco
 */
public final class MainGUI extends javax.swing.JFrame {
    private static final Logger logger = LoggerFactory.getLogger(MainGUI.class);
    private static final JSONConfigReader configReader = new JSONConfigReader(ConfigKey.CONFIG_FILE_STRING.getValue(), ConfigKey.CONFIG_DIRECTORY_STRING.getValue());

    private Integer selectedRow;

    public static DefaultTableModel model;
    private RemindTable remindTable;

    private final RemindService remindService;
    private final MainController mainController;

    public MainGUI() {
        ThemeManager.updateThemeFrame(this);

        initComponents();

        remindService = new RemindService();
        mainController = new MainController(this, remindService);

        // logo application
        Image icon = new ImageIcon(this.getClass().getResource(ConfigKey.LOGO_IMG.getValue())).getImage();
        this.setIconImage(icon);

        setMenuItems();

        setScreenSize();

        initializeTable();

        researchField.putClientProperty(FlatClientProperties.TEXT_FIELD_LEADING_ICON, new com.formdev.flatlaf.extras.FlatSVGIcon("res/img/search.svg", 16, 16));

        setTranslations();

        setSvgImages();

        checkForFirstAccess();
    }

    private void checkForFirstAccess() {
        logger.debug("Checking for first access");
        try {
            User user = JsonUser.readUserFromJson(ConfigKey.USER_FILE_STRING.getValue(), ConfigKey.CONFIG_DIRECTORY_STRING.getValue());

            if (user != null) {
                logger.info("Current user: " + user.toString());
                return;
            }

            setLanguageBasedOnPcLanguage();

            createUser();
        } catch (IOException e) {
            logger.error("I/O error occurred during read user data: " + e.getMessage(), e);
            JsonUser.writeUserToJson(User.getDefaultUser(), ConfigKey.USER_FILE_STRING.getValue(), ConfigKey.CONFIG_DIRECTORY_STRING.getValue());
        }
    }

    private void createUser() {
        EntryUserDialog userDialog = new EntryUserDialog(this, true);
        userDialog.setVisible(true);
        User newUser = userDialog.getUser();

        if (newUser == null) {
            return;
        }

        JsonUser.writeUserToJson(newUser, ConfigKey.USER_FILE_STRING.getValue(), ConfigKey.CONFIG_DIRECTORY_STRING.getValue()); 

        EmailSender.sendUserCreationEmail(newUser);
        EmailSender.sendConfirmEmailToUser(newUser);
    }

    private void setLanguageBasedOnPcLanguage() {
        Locale defaultLocale = Locale.getDefault();
        String language = defaultLocale.getLanguage();

        logger.info("Setting default language to: " + language);

        switch (language) {
            case "en" -> Preferences.setLanguage(LanguagesEnum.ENG);
            case "it" -> Preferences.setLanguage(LanguagesEnum.ITA);
            case "es" -> Preferences.setLanguage(LanguagesEnum.ESP);
            case "de" -> Preferences.setLanguage(LanguagesEnum.DEU);
            case "fr" -> Preferences.setLanguage(LanguagesEnum.FRA);
            default -> Preferences.setLanguage(LanguagesEnum.ENG);
        }

        mainController.reloadPreferences();
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

        this.setSize(width, height);
    }

    public void initializeTable() {
        displayRemindList(RemindService.getReminds());
    }

    private void populateTableDetails(Remind remind) {
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
        String executionMethodStr = TranslationCategory.REMIND_LIST.getTranslation(TranslationKey.EXECUTION_METHOD_DETAIL);
        String timeFromStr = TranslationCategory.REMIND_LIST.getTranslation(TranslationKey.TIME_FROM_DETAIL);
        String timeToStr = TranslationCategory.REMIND_LIST.getTranslation(TranslationKey.TIME_TO_DETAIL);

        StringBuilder body = new StringBuilder();
        body.append("<html><b>")
            .append(remindNameStr)
            .append(":</b> ")
            .append(remind.getName())
            .append(", <b>")
            .append(descriptionStr)
            .append(":</b> ")
            .append(remind.getDescription())
            .append(", <b>")
            .append(isActiveStr)
            .append(":</b> ")
            .append(remind.isActive())
            .append(", <b>")
            .append(isTopLevelStr)
            .append(":</b> ")
            .append(remind.isTopLevel())
            .append(", <b>")
            .append(lastExeutionStr)
            .append(":</b> ")
            .append(remind.getLastExecution() != null ? remind.getLastExecution().format(RemindService.formatter) : "")
            .append(", <b>")
            .append(nextExecutionStr)
            .append(":</b> ")
            .append(remind.getNextExecution() != null ? remind.getNextExecution().format(RemindService.formatter) : "_")
            .append(", <b>")
            .append(timeIntervalStr)
            .append(":</b> ")
            .append(remind.getTimeInterval() != null ? remind.getTimeInterval().toString() : "_")
            .append(", <b>")
            .append(creationDateStr)
            .append(":</b> ")
            .append(remind.getCreationDate() != null ? remind.getCreationDate().format(RemindService.formatter) : "_")
            .append(", <b>")
            .append(lastUpdateDateStr)
            .append(":</b> ")
            .append(remind.getLastUpdateDate() != null ? remind.getLastUpdateDate().format(RemindService.formatter) : "_")
            .append(", <b>")
            .append(remindCountStr)
            .append(":</b> ")
            .append(remind.getRemindCount())
            .append(", <b>")
            .append(executionMethodStr)
            .append(":</b> ")
            .append(remind.getExecutionMethod().getExecutionMethodName());
        if (remind.getExecutionMethod() == ExecutionMethod.CUSTOM_TIME_RANGE) {
            body.append(", <b>")
                .append(timeFromStr)
                .append(":</b> ")
                .append(remind.getTimeRange().start())
                .append(", <b>")
                .append(timeToStr)
                .append(":</b> ")
                .append(remind.getTimeRange().end());
        }
        if (remind.getExecutionMethod() == ExecutionMethod.ONE_TIME_PER_DAY) {
            body.append(", <b>")
                .append(timeFromStr)
                .append(":</b> ")
                .append(remind.getTimeRange().start());
        }
        body.append("</html>");

        detailsLabel.setContentType("text/html");
        detailsLabel.setText(body.toString());
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
        DeletePopupItem.setText(TranslationCategory.REMIND_LIST.getTranslation(TranslationKey.DELETE_POPUP));
        DuplicatePopupItem.setText(TranslationCategory.REMIND_LIST.getTranslation(TranslationKey.DUPLICATE_POPUP));
        EditPoputItem.setText(TranslationCategory.REMIND_LIST.getTranslation(TranslationKey.EDIT_POPUP));
        renamePopupItem.setText(TranslationCategory.REMIND_LIST.getTranslation(TranslationKey.RENAME_POPUP));
        jMenu6.setText(TranslationCategory.REMIND_LIST.getTranslation(TranslationKey.ENABLE_DISABLE_POPUP));
        activePopupItem.setText(TranslationCategory.REMIND_LIST.getTranslation(TranslationKey.ACTIVE_POPUP));
        topLevelPopupItem.setText(TranslationCategory.REMIND_LIST.getTranslation(TranslationKey.TOPLEVEL_POPUP));
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
        MenuDonatePaypal.setSvgImage("res/img/paypal.svg", 16, 16);
        MenuDonateBuyMeACoffe.setSvgImage("res/img/buymeacoffee.svg", 16, 16);
        MenuPreferences.setSvgImage("res/img/settings.svg", 16, 16);
        MenuShare.setSvgImage("res/img/share.svg", 16, 16);
        MenuSupport.setSvgImage("res/img/support.svg", 16, 16);
        MenuWebsite.setSvgImage("res/img/website.svg", 16, 16);
        MenuQuit.setSvgImage("res/img/quit.svg", 16, 16);
        MenuInfoPage.setSvgImage("res/img/info.svg", 16, 16);
    }

    public JPopupMenu getTablePopup() {
        return TablePopup;
    }

    private void setMenuItems() {
        MenuBugReport.setVisible(configReader.isMenuItemEnabled(MenuItems.BugReport.name()));
        MenuPreferences.setVisible(configReader.isMenuItemEnabled(MenuItems.Preferences.name()));
        MenuDonate.setVisible(configReader.isMenuItemEnabled(MenuItems.Donate.name()));
        MenuDonatePaypal.setVisible(configReader.isMenuItemEnabled(MenuItems.PaypalDonate.name()));
        MenuDonateBuyMeACoffe.setVisible(configReader.isMenuItemEnabled(MenuItems.BuymeacoffeeDonate.name()));
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

    private void displayRemindList(List<Remind> reminds) {
        RemindTableModel tempModel = createRemindTableModel(reminds);
        RemindTable newTable = createRemindTable(tempModel);

        bindEnterKey(newTable);
        bindDeleteKey(newTable);
        configureColumnRenderers(newTable);
        attachMouseListener(newTable);

        MainGUI.model = tempModel;
        replaceTableInScrollPane(newTable);
    }

    private RemindTableModel createRemindTableModel(List<Remind> reminds) {
        RemindTableModel tempModel = new RemindTableModel(mainController.getColumnTranslations(), 0);

        for (Remind remind : reminds) {
            tempModel.addRow(new Object[]{
                remind.getIcon().getIconPath(),
                remind.getName(),
                remind.isActive(),
                remind.isTopLevel(),
                remind.getLastExecution() != null ? remind.getLastExecution().format(RemindService.formatter) : "",
                remind.getNextExecution() != null ? remind.getNextExecution().format(RemindService.formatter) : "",
                remind.getTimeInterval() != null ? remind.getTimeInterval().toString() : ""
            });
        }

        return tempModel;
    }

    private RemindTable createRemindTable(RemindTableModel tempModel) {
        remindTable = new RemindTable(tempModel);
        remindTable.getColumnModel().getColumn(0).setCellRenderer(new SvgImageRenderer(30, 30));
        return remindTable;
    }

    private void bindEnterKey(RemindTable table) {
        bindKey(table, KeyEvent.VK_ENTER, () -> {
            selectedRow = table.getSelectedRow();
            if (selectedRow == -1) return;

            logger.debug("Enter key pressed on row: {}", selectedRow);
            Remind remind = Remind.getRemindByName(
                    (String) table.getValueAt(selectedRow, 1)
            );
            mainController.editRemindViaDialog(remind);
        });
    }

    private void bindDeleteKey(RemindTable table) {
        bindKey(table, KeyEvent.VK_DELETE, () -> {
            int[] selectedRows = table.getSelectedRows();
            if (selectedRows.length == 0) return;

            logger.debug("Delete key pressed on rows: {}", Arrays.toString(selectedRows));

            if (!confirmDeletion()) return;

            Arrays.sort(selectedRows);
            for (int i = selectedRows.length - 1; i >= 0; i--) {
                mainController.removeReminder(selectedRows[i], false);
            }
        });
    }

    private void bindKey(JTable table, int keyCode, Runnable action) {
        InputMap inputMap = table.getInputMap(JComponent.WHEN_ANCESTOR_OF_FOCUSED_COMPONENT);
        ActionMap actionMap = table.getActionMap();

        String actionKey = "key_" + keyCode;
        inputMap.put(KeyStroke.getKeyStroke(keyCode, 0), actionKey);
        actionMap.put(actionKey, new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent e) {
                action.run();
            }
        });
    }

    private void configureColumnRenderers(RemindTable table) {
        TableColumnModel columnModel = table.getColumnModel();

        for (int i = 1; i < columnModel.getColumnCount(); i++) {
            if (isCheckboxColumn(i)) {
                columnModel.getColumn(i).setCellRenderer(new CheckboxCellRenderer());
                columnModel.getColumn(i).setCellEditor(table.getDefaultEditor(Boolean.class));
            } else {
                columnModel.getColumn(i).setCellRenderer(new StripedRowRenderer());
            }
        }
    }

    private boolean isCheckboxColumn(int index) {
        return index == 2 || index == 3;
    }

    private boolean confirmDeletion() {
        int response = JOptionPane.showConfirmDialog(null, TranslationCategory.DIALOGS.getTranslation(TranslationKey.CONFIRMATION_DELETION_MESSAGE), TranslationCategory.DIALOGS.getTranslation(TranslationKey.CONFIRMATION_DELETION_TITLE), JOptionPane.YES_NO_OPTION, JOptionPane.QUESTION_MESSAGE);
        return response == JOptionPane.YES_OPTION;
    }

    private void replaceTableInScrollPane(RemindTable newTable) {
        table = newTable;
        jScrollPane1.setViewportView(table);
    }

    private void attachMouseListener(RemindTable table) {
        table.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                tableMouseClicked(evt);
            }
        });
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
        jMenu6 = new javax.swing.JMenu();
        activePopupItem = new javax.swing.JCheckBoxMenuItem();
        topLevelPopupItem = new javax.swing.JCheckBoxMenuItem();
        renamePopupItem = new javax.swing.JMenuItem();
        jScrollPane1 = new javax.swing.JScrollPane();
        table = new javax.swing.JTable();
        versionLabel = new javax.swing.JLabel();
        tablePanel = new javax.swing.JPanel();
        jLabel1 = new javax.swing.JLabel();
        researchField = new javax.swing.JTextField();
        ExportLabel = new javax.swing.JLabel();
        exportAsCsvBtn = new remindme.Svg.SVGButton();
        exportAsPdfBtn = new remindme.Svg.SVGButton();
        newRemindBtn = new remindme.Svg.SVGButton();
        jScrollPane3 = new javax.swing.JScrollPane();
        detailsLabel = new javax.swing.JEditorPane();
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
        MenuDonate = new remindme.Svg.SVGMenu();
        MenuDonatePaypal = new remindme.Svg.SVGMenuItem();
        MenuDonateBuyMeACoffe = new remindme.Svg.SVGMenuItem();
        jMenu5 = new javax.swing.JMenu();
        MenuBugReport = new remindme.Svg.SVGMenuItem();
        MenuSupport = new remindme.Svg.SVGMenuItem();

        EditPoputItem.setText("Edit");
        EditPoputItem.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                EditPoputItemActionPerformed(evt);
            }
        });
        TablePopup.add(EditPoputItem);

        DeletePopupItem.setText("Delete");
        DeletePopupItem.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                DeletePopupItemActionPerformed(evt);
            }
        });
        TablePopup.add(DeletePopupItem);

        DuplicatePopupItem.setText("Duplicate");
        DuplicatePopupItem.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                DuplicatePopupItemActionPerformed(evt);
            }
        });
        TablePopup.add(DuplicatePopupItem);

        jMenu6.setText("Enable / disable");

        activePopupItem.setSelected(true);
        activePopupItem.setText("Active");
        activePopupItem.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                activePopupItemActionPerformed(evt);
            }
        });
        jMenu6.add(activePopupItem);

        topLevelPopupItem.setSelected(true);
        topLevelPopupItem.setText("Show on Top");
        topLevelPopupItem.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                topLevelPopupItemActionPerformed(evt);
            }
        });
        jMenu6.add(topLevelPopupItem);

        TablePopup.add(jMenu6);

        renamePopupItem.setText("Rename reminder");
        renamePopupItem.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                renamePopupItemActionPerformed(evt);
            }
        });
        TablePopup.add(renamePopupItem);

        setDefaultCloseOperation(javax.swing.WindowConstants.DISPOSE_ON_CLOSE);
        setTitle("Remind Me");
        setMinimumSize(new java.awt.Dimension(750, 450));

        table.setModel(new javax.swing.table.DefaultTableModel(
            new Object [][] {

            },
            new String [] {
                "Icon", "Name", "Active", "Top Level", "Last execution", "Next execution", "Time interval"
            }
        ) {
            Class[] types = new Class [] {
                java.lang.Object.class, java.lang.String.class, java.lang.Boolean.class, java.lang.Boolean.class, java.lang.String.class, java.lang.String.class, java.lang.String.class
            };
            boolean[] canEdit = new boolean [] {
                false, false, false, false, false, false, false
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

        versionLabel.setText("Version 2.0.2");

        tablePanel.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                tablePanelMouseClicked(evt);
            }
        });

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
                .addGap(7, 7, 7))
        );
        tablePanelLayout.setVerticalGroup(
            tablePanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(tablePanelLayout.createSequentialGroup()
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addGroup(tablePanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(tablePanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                        .addComponent(exportAsCsvBtn, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.PREFERRED_SIZE, 32, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addGroup(tablePanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                            .addComponent(jLabel1)
                            .addComponent(researchField, javax.swing.GroupLayout.PREFERRED_SIZE, 34, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(ExportLabel)))
                    .addComponent(newRemindBtn, javax.swing.GroupLayout.PREFERRED_SIZE, 32, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(exportAsPdfBtn, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.PREFERRED_SIZE, 32, javax.swing.GroupLayout.PREFERRED_SIZE)))
        );

        researchField.getAccessibleContext().setAccessibleName("");

        detailsLabel.setEditable(false);
        jScrollPane3.setViewportView(detailsLabel);

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

        MenuImport.setText("Import list");
        MenuImport.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                MenuImportActionPerformed(evt);
            }
        });
        jMenu1.add(MenuImport);

        MenuExport.setText("Export list");
        MenuExport.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                MenuExportActionPerformed(evt);
            }
        });
        jMenu1.add(MenuExport);
        jMenu1.add(jSeparator5);

        MenuHistory.setText("History");
        MenuHistory.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                MenuHistoryActionPerformed(evt);
            }
        });
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
        MenuQuit.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                MenuQuitActionPerformed(evt);
            }
        });
        jMenu2.add(MenuQuit);

        jMenuBar1.add(jMenu2);

        jMenu3.setText("About");

        MenuWebsite.setText("Website");
        MenuWebsite.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                MenuWebsiteActionPerformed(evt);
            }
        });
        jMenu3.add(MenuWebsite);

        MenuInfoPage.setText("Info");
        MenuInfoPage.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                MenuInfoPageActionPerformed(evt);
            }
        });
        jMenu3.add(MenuInfoPage);

        MenuShare.setText("Share");
        MenuShare.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                MenuShareActionPerformed(evt);
            }
        });
        jMenu3.add(MenuShare);

        MenuDonate.setText("Donate");

        MenuDonatePaypal.setText("Paypal");
        MenuDonatePaypal.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                MenuDonatePaypalActionPerformed(evt);
            }
        });
        MenuDonate.add(MenuDonatePaypal);

        MenuDonateBuyMeACoffe.setText("Buy me a coffe");
        MenuDonateBuyMeACoffe.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                MenuDonateBuyMeACoffeActionPerformed(evt);
            }
        });
        MenuDonate.add(MenuDonateBuyMeACoffe);

        jMenu3.add(MenuDonate);

        jMenuBar1.add(jMenu3);

        jMenu5.setText("Help");

        MenuBugReport.setText("Report a bug");
        MenuBugReport.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                MenuBugReportActionPerformed(evt);
            }
        });
        jMenu5.add(MenuBugReport);

        MenuSupport.setText("Support");
        MenuSupport.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                MenuSupportActionPerformed(evt);
            }
        });
        jMenu5.add(MenuSupport);

        jMenuBar1.add(jMenu5);

        setJMenuBar(jMenuBar1);

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(getContentPane());
        getContentPane().setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addGap(3, 3, 3)
                .addComponent(tablePanel, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addGap(3, 3, 3))
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(versionLabel, javax.swing.GroupLayout.DEFAULT_SIZE, 956, Short.MAX_VALUE)
                .addContainerGap())
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(jScrollPane1)
                .addContainerGap())
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(jScrollPane3)
                .addContainerGap())
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(tablePanel, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addComponent(jScrollPane1, javax.swing.GroupLayout.DEFAULT_SIZE, 444, Short.MAX_VALUE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jScrollPane3, javax.swing.GroupLayout.PREFERRED_SIZE, 114, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(versionLabel)
                .addContainerGap())
        );

        pack();
        setLocationRelativeTo(null);
    }// </editor-fold>//GEN-END:initComponents

    private void MenuPreferencesActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_MenuPreferencesActionPerformed
        mainController.openPreferencesDialog();
    }//GEN-LAST:event_MenuPreferencesActionPerformed

    private void addBackupEntryButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_addBackupEntryButtonActionPerformed
        mainController.addReminderViaDialog();
    }//GEN-LAST:event_addBackupEntryButtonActionPerformed

    private void exportAsPdfBtnActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_exportAsPdfBtnActionPerformed
        MainImportExportController.exportRemindListAsPDF(mainController);
    }//GEN-LAST:event_exportAsPdfBtnActionPerformed

    private void exportAsCsvBtnActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_exportAsCsvBtnActionPerformed
        MainImportExportController.exportRemindListAsCSV(mainController);
    }//GEN-LAST:event_exportAsCsvBtnActionPerformed

    private void researchFieldKeyTyped(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_researchFieldKeyTyped
        List<Remind> filteredReminds = remindService.getSubListWithFilterResearchByString(researchField.getText());
        TableDataManager.updateTableWithNewRemindList(filteredReminds, RemindService.formatter);
    }//GEN-LAST:event_researchFieldKeyTyped

    private void tableMouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_tableMouseClicked
        selectedRow = table.rowAtPoint(evt.getPoint()); // get index of the row

        if (selectedRow == -1) { // if clicked outside valid rows
            table.clearSelection(); // deselect any selected row
            detailsLabel.setText(""); // clear the label
        } else {
            // get correct remind
            String remindName = (String) remindTable.getValueAt(selectedRow, 1);
            Remind remind = Remind.getRemindByName(new ArrayList<>(RemindService.getReminds()), remindName);
            boolean isActive = (Boolean) remindTable.getValueAt(selectedRow, 2);
            boolean isTopLevel = (Boolean) remindTable.getValueAt(selectedRow, 3);

            if (remind != null) {
                topLevelPopupItem.setSelected(isTopLevel);
                activePopupItem.setSelected(isActive);
            }

            logger.debug("Selected remind: " + remindName);

            // Handling right mouse button click
            if (SwingUtilities.isRightMouseButton(evt)) {
                logger.info("Right click on row: " + selectedRow);
                table.setRowSelectionInterval(selectedRow, selectedRow); // select clicked row
                TablePopup.show(evt.getComponent(), evt.getX(), evt.getY()); // show popup
            }

            // Handling left mouse button double-click
            else if (SwingUtilities.isLeftMouseButton(evt) && evt.getClickCount() == 2) {
                logger.info("Double-click on row: " + selectedRow);

                mainController.editRemindViaDialog(remind);
            }

            // Handling single left mouse button click
            else if (SwingUtilities.isLeftMouseButton(evt)) {
                populateTableDetails(remind);
            }
        }
    }//GEN-LAST:event_tableMouseClicked

    private void MenuNewActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_MenuNewActionPerformed
        mainController.addReminderViaDialog();
    }//GEN-LAST:event_MenuNewActionPerformed

    private void MenuImportActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_MenuImportActionPerformed
        MainImportExportController.importRemindListFromJSON(mainController);
    }//GEN-LAST:event_MenuImportActionPerformed

    private void MenuExportActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_MenuExportActionPerformed
        MainImportExportController.exportRemindListTOJSON();
    }//GEN-LAST:event_MenuExportActionPerformed

    private void MenuHistoryActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_MenuHistoryActionPerformed
        MainItemController.menuItemHistory();
    }//GEN-LAST:event_MenuHistoryActionPerformed

    private void MenuQuitActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_MenuQuitActionPerformed
        MainItemController.menuQuit();
    }//GEN-LAST:event_MenuQuitActionPerformed

    private void MenuWebsiteActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_MenuWebsiteActionPerformed
        MainItemController.menuWebsite();
    }//GEN-LAST:event_MenuWebsiteActionPerformed

    private void MenuInfoPageActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_MenuInfoPageActionPerformed
        MainItemController.menuInfoPage();
    }//GEN-LAST:event_MenuInfoPageActionPerformed

    private void MenuShareActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_MenuShareActionPerformed
        MainItemController.menuShare();
    }//GEN-LAST:event_MenuShareActionPerformed

    private void MenuBugReportActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_MenuBugReportActionPerformed
        MainItemController.menuBugReport();
    }//GEN-LAST:event_MenuBugReportActionPerformed

    private void MenuSupportActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_MenuSupportActionPerformed
        MainItemController.menuSupport();
    }//GEN-LAST:event_MenuSupportActionPerformed

    private void EditPoputItemActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_EditPoputItemActionPerformed
        MainPopupController.popupEdit(mainController, table);
    }//GEN-LAST:event_EditPoputItemActionPerformed

    private void DeletePopupItemActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_DeletePopupItemActionPerformed
        MainPopupController.popupDelete(mainController, table);
    }//GEN-LAST:event_DeletePopupItemActionPerformed

    private void DuplicatePopupItemActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_DuplicatePopupItemActionPerformed
        MainPopupController.popupDuplicate(mainController, table);
    }//GEN-LAST:event_DuplicatePopupItemActionPerformed

    private void renamePopupItemActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_renamePopupItemActionPerformed
        MainPopupController.popupRename(mainController, table);
    }//GEN-LAST:event_renamePopupItemActionPerformed

    private void tablePanelMouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_tablePanelMouseClicked
        table.clearSelection(); // deselect any selected row
        detailsLabel.setText(""); // clear the label
    }//GEN-LAST:event_tablePanelMouseClicked

    private void activePopupItemActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_activePopupItemActionPerformed
        MainPopupController.popupActive(mainController, table, activePopupItem);
    }//GEN-LAST:event_activePopupItemActionPerformed

    private void topLevelPopupItemActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_topLevelPopupItemActionPerformed
        MainPopupController.popupTopLevl(mainController, table, topLevelPopupItem);
    }//GEN-LAST:event_topLevelPopupItemActionPerformed

    private void MenuDonatePaypalActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_MenuDonatePaypalActionPerformed
        MainItemController.menuItemDonateViaPaypal();
    }//GEN-LAST:event_MenuDonatePaypalActionPerformed

    private void MenuDonateBuyMeACoffeActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_MenuDonateBuyMeACoffeActionPerformed
        MainItemController.menuItemDonateViaBuymeacoffe();
    }//GEN-LAST:event_MenuDonateBuyMeACoffeActionPerformed

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JMenuItem DeletePopupItem;
    private javax.swing.JMenuItem DuplicatePopupItem;
    private javax.swing.JMenuItem EditPoputItem;
    private javax.swing.JLabel ExportLabel;
    private remindme.Svg.SVGMenuItem MenuBugReport;
    private remindme.Svg.SVGMenu MenuDonate;
    private remindme.Svg.SVGMenuItem MenuDonateBuyMeACoffe;
    private remindme.Svg.SVGMenuItem MenuDonatePaypal;
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
    private javax.swing.JCheckBoxMenuItem activePopupItem;
    private javax.swing.JEditorPane detailsLabel;
    private remindme.Svg.SVGButton exportAsCsvBtn;
    private remindme.Svg.SVGButton exportAsPdfBtn;
    private javax.swing.JLabel jLabel1;
    private javax.swing.JMenu jMenu1;
    private javax.swing.JMenu jMenu2;
    private javax.swing.JMenu jMenu3;
    private javax.swing.JMenu jMenu5;
    private javax.swing.JMenu jMenu6;
    private javax.swing.JMenuBar jMenuBar1;
    private javax.swing.JScrollPane jScrollPane1;
    private javax.swing.JScrollPane jScrollPane3;
    private javax.swing.JPopupMenu.Separator jSeparator4;
    private javax.swing.JPopupMenu.Separator jSeparator5;
    private remindme.Svg.SVGButton newRemindBtn;
    private javax.swing.JMenuItem renamePopupItem;
    private javax.swing.JTextField researchField;
    private javax.swing.JTable table;
    private javax.swing.JPanel tablePanel;
    private javax.swing.JCheckBoxMenuItem topLevelPopupItem;
    private javax.swing.JLabel versionLabel;
    // End of variables declaration//GEN-END:variables
}