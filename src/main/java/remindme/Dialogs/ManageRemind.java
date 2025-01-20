package remindme.Dialogs;

import remindme.Entities.Remind;
import remindme.Entities.RemindNotification;
import remindme.Enums.IconsEnum;
import remindme.Enums.SoundsEnum;
import remindme.Enums.TranslationLoaderEnum.TranslationCategory;
import remindme.Enums.TranslationLoaderEnum.TranslationKey;
import remindme.Managers.SoundPlayer;

public class ManageRemind extends javax.swing.JDialog {
    
    public ManageRemind(java.awt.Frame parent, boolean modal, String title, String confirmBtnName, Remind remind) {
        super(parent, modal);
        initializeDialog(title, confirmBtnName);
        insertRemindValues(remind);
    }

    public ManageRemind(java.awt.Frame parent, boolean modal, String title, String confirmBtnName) {
        super(parent, modal);
        initializeDialog(title, confirmBtnName);
    }

    private void initializeDialog(String title, String confirmBtnName) {
        initComponents();
        setSvgImages();
        setTitle(title);
        OkBtn.setText(confirmBtnName);

        setIcons();
        setSounds();

        setTranslations();
    }

    private void insertRemindValues(Remind remind) {
        remindNameTextField.setText(remind.getName());
        descriptionTextArea.setText(remind.getDescription());
        activeCheckBox.setSelected(remind.isActive());
        topLevelCheckBox.setSelected(remind.isTopLevel());
        iconComboBox.setSelectedItem(remind.getIcon().getIconName());
        soundComboBox.setSelectedItem(remind.getSound().getSoundName());
    }
    
    public void setSvgImages() {
        soundPreviewBtn.setSvgImage("res/img/sound.svg", 35, 35);
        timeIntervalBtn.setSvgImage("res/img/timer.svg", 50, 50);
    }

    private void setTranslations() {
        remindNameTextField.setText(TranslationCategory.MANAGE_REMIND_DIALOG.getTranslation(TranslationKey.NAME_TEXT));
        activeCheckBox.setText(TranslationCategory.MANAGE_REMIND_DIALOG.getTranslation(TranslationKey.ACTIVE_TEXT));
        topLevelCheckBox.setText(TranslationCategory.MANAGE_REMIND_DIALOG.getTranslation(TranslationKey.TOP_LEVEL_TEXT));
        reminderPreviewBtn.setText(TranslationCategory.MANAGE_REMIND_DIALOG.getTranslation(TranslationKey.PREVIEW_TEXT));

        // remindNameTextField.setPlaceholder(TranslationCategory.MANAGE_REMIND_DIALOG.getTranslation(TranslationKey.NAME_PLACEHOLDER));
        // descriptionTextArea.setPlaceholder(TranslationCategory.MANAGE_REMIND_DIALOG.getTranslation(TranslationKey.DESCRIPTION_PLACEHOLDER));
        
        remindNameTextField.setToolTipText(TranslationCategory.MANAGE_REMIND_DIALOG.getTranslation(TranslationKey.NAME_TOOLTIP));
        descriptionTextArea.setToolTipText(TranslationCategory.MANAGE_REMIND_DIALOG.getTranslation(TranslationKey.DESCRIPTION_TOOLTIP));
        activeCheckBox.setToolTipText(TranslationCategory.MANAGE_REMIND_DIALOG.getTranslation(TranslationKey.ACTIVE_TOOLTIP));
        topLevelCheckBox.setToolTipText(TranslationCategory.MANAGE_REMIND_DIALOG.getTranslation(TranslationKey.TOP_LEVEL_TOOLTIP));
        iconComboBox.setToolTipText(TranslationCategory.MANAGE_REMIND_DIALOG.getTranslation(TranslationKey.ICON_TOOLTIP));
        soundComboBox.setToolTipText(TranslationCategory.MANAGE_REMIND_DIALOG.getTranslation(TranslationKey.SOUND_TOOLTIP));
        soundPreviewBtn.setToolTipText(TranslationCategory.MANAGE_REMIND_DIALOG.getTranslation(TranslationKey.SOUND_BUTTON_TOOLTIP));
    }

    private void setIcons() {
        iconComboBox.removeAllItems();

        iconComboBox.addItem(IconsEnum.ALERT.getIconName());
        iconComboBox.addItem(IconsEnum.BOOK.getIconName());
        iconComboBox.addItem(IconsEnum.BOOK_CLOSED.getIconName());
        iconComboBox.addItem(IconsEnum.EYE.getIconName());
        iconComboBox.addItem(IconsEnum.EYE_CLOSED.getIconName());
        iconComboBox.addItem(IconsEnum.MAN.getIconName());
        iconComboBox.addItem(IconsEnum.MAN_BEER.getIconName());
        iconComboBox.addItem(IconsEnum.MAN.getIconName());
        iconComboBox.addItem(IconsEnum.MAN_CALCULATOR.getIconName());
        iconComboBox.addItem(IconsEnum.MAN_COMPUTER.getIconName());
        iconComboBox.addItem(IconsEnum.MAN_JOGGING.getIconName());
        iconComboBox.addItem(IconsEnum.MAN_SHOPPING.getIconName());
        iconComboBox.addItem(IconsEnum.MAN_SLEEPING.getIconName());
        iconComboBox.addItem(IconsEnum.MAN_WEARING_TIE.getIconName());
        iconComboBox.addItem(IconsEnum.MAN_WITH_DIETARY.getIconName());
        iconComboBox.addItem(IconsEnum.MAN_WITH_DIETARY.getIconName());
        iconComboBox.addItem(IconsEnum.MAN_YOGA.getIconName());
        iconComboBox.addItem(IconsEnum.MUSIC1.getIconName());
        iconComboBox.addItem(IconsEnum.MUSIC2.getIconName());
        iconComboBox.addItem(IconsEnum.PAUSE_CIRCLE.getIconName());
        iconComboBox.addItem(IconsEnum.WARNING.getIconName());
        iconComboBox.addItem(IconsEnum.WORK.getIconName());
        
        iconComboBox.setSelectedItem(IconsEnum.getDefaultIcon());
    }
    
    private void setSounds() {
        soundComboBox.removeAllItems();

        soundComboBox.addItem(SoundsEnum.NoSound.getSoundName());
        soundComboBox.addItem(SoundsEnum.Sound1.getSoundName());
        soundComboBox.addItem(SoundsEnum.Sound2.getSoundName());
        soundComboBox.addItem(SoundsEnum.Sound3.getSoundName());
        soundComboBox.addItem(SoundsEnum.Sound4.getSoundName());
        soundComboBox.addItem(SoundsEnum.Sound5.getSoundName());
        // soundComboBox.addItem(SoundsEnum.Sound6.getSoundName());
        // soundComboBox.addItem(SoundsEnum.Sound7.getSoundName());
        soundComboBox.addItem(SoundsEnum.Sound8.getSoundName());
        soundComboBox.addItem(SoundsEnum.Sound9.getSoundName());
        // soundComboBox.addItem(SoundsEnum.Sound10.getSoundName());
        soundComboBox.addItem(SoundsEnum.Sound11.getSoundName());
        soundComboBox.addItem(SoundsEnum.Sound12.getSoundName());
        
        soundComboBox.setSelectedItem(SoundsEnum.getDefaultSound());
    }

    /**
     * This method is called from within the constructor to initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is always
     * regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        remindNameTextField = new remindme.customwidgets.ModernTextField();
        activeCheckBox = new javax.swing.JCheckBox();
        topLevelCheckBox = new javax.swing.JCheckBox();
        cancelBtn = new javax.swing.JButton();
        OkBtn = new javax.swing.JButton();
        soundComboBox = new javax.swing.JComboBox<>();
        jScrollPane2 = new javax.swing.JScrollPane();
        descriptionTextArea = new javax.swing.JTextArea();
        iconComboBox = new javax.swing.JComboBox<>();
        reminderPreviewBtn = new javax.swing.JButton();
        jLabel1 = new javax.swing.JLabel();
        timeIntervalBtn = new remindme.Svg.SVGButton();
        soundPreviewBtn = new remindme.Svg.SVGButton();

        setDefaultCloseOperation(javax.swing.WindowConstants.DISPOSE_ON_CLOSE);
        setMinimumSize(new java.awt.Dimension(429, 400));
        setResizable(false);

        remindNameTextField.setLabelText("Name");

        activeCheckBox.setText("Active");

        topLevelCheckBox.setText("Top Level");

        cancelBtn.setText("Cancel");
        cancelBtn.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                cancelBtnActionPerformed(evt);
            }
        });

        OkBtn.setText("Ok");
        OkBtn.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                OkBtnActionPerformed(evt);
            }
        });

        soundComboBox.setModel(new javax.swing.DefaultComboBoxModel<>(new String[] { "Item 1", "Item 2", "Item 3", "Item 4" }));

        descriptionTextArea.setColumns(20);
        descriptionTextArea.setRows(5);
        descriptionTextArea.setToolTipText("");
        jScrollPane2.setViewportView(descriptionTextArea);

        iconComboBox.setModel(new javax.swing.DefaultComboBoxModel<>(new String[] { "Item 1", "Item 2", "Item 3", "Item 4" }));
        iconComboBox.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                iconComboBoxActionPerformed(evt);
            }
        });

        reminderPreviewBtn.setText("Reminder preview");
        reminderPreviewBtn.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                reminderPreviewBtnActionPerformed(evt);
            }
        });

        jLabel1.setText("jLabel1");

        soundPreviewBtn.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                soundPreviewBtnActionPerformed(evt);
            }
        });

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(getContentPane());
        getContentPane().setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jScrollPane2, javax.swing.GroupLayout.Alignment.TRAILING)
                    .addComponent(activeCheckBox, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(topLevelCheckBox, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, layout.createSequentialGroup()
                        .addGap(0, 0, Short.MAX_VALUE)
                        .addComponent(OkBtn)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(cancelBtn))
                    .addComponent(remindNameTextField, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(iconComboBox, 0, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addGroup(layout.createSequentialGroup()
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addGroup(layout.createSequentialGroup()
                                .addComponent(timeIntervalBtn, javax.swing.GroupLayout.PREFERRED_SIZE, 50, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addComponent(jLabel1, javax.swing.GroupLayout.PREFERRED_SIZE, 170, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addGap(0, 0, Short.MAX_VALUE))
                            .addComponent(soundComboBox, 0, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(soundPreviewBtn, javax.swing.GroupLayout.PREFERRED_SIZE, 41, javax.swing.GroupLayout.PREFERRED_SIZE)))
                .addContainerGap())
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, layout.createSequentialGroup()
                .addContainerGap(120, Short.MAX_VALUE)
                .addComponent(reminderPreviewBtn, javax.swing.GroupLayout.PREFERRED_SIZE, 226, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(111, 111, 111))
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(remindNameTextField, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(11, 11, 11)
                .addComponent(iconComboBox, javax.swing.GroupLayout.PREFERRED_SIZE, 41, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                    .addComponent(soundComboBox, javax.swing.GroupLayout.DEFAULT_SIZE, 41, Short.MAX_VALUE)
                    .addComponent(soundPreviewBtn, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(activeCheckBox, javax.swing.GroupLayout.PREFERRED_SIZE, 38, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(topLevelCheckBox, javax.swing.GroupLayout.PREFERRED_SIZE, 38, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jScrollPane2, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jLabel1, javax.swing.GroupLayout.PREFERRED_SIZE, 50, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(timeIntervalBtn, javax.swing.GroupLayout.PREFERRED_SIZE, 50, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addGap(18, 18, 18)
                .addComponent(reminderPreviewBtn)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, 22, Short.MAX_VALUE)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(OkBtn)
                    .addComponent(cancelBtn))
                .addContainerGap())
        );

        pack();
        setLocationRelativeTo(null);
    }// </editor-fold>//GEN-END:initComponents

    private void soundPreviewBtnActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_soundPreviewBtnActionPerformed
        String sound = (String) soundComboBox.getSelectedItem();
        SoundPlayer.playSound(SoundsEnum.getSoundbyName(sound));
    }//GEN-LAST:event_soundPreviewBtnActionPerformed

    private void iconComboBoxActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_iconComboBoxActionPerformed
        // TODO add your handling code here:
    }//GEN-LAST:event_iconComboBoxActionPerformed

    private void cancelBtnActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_cancelBtnActionPerformed
        this.dispose();
    }//GEN-LAST:event_cancelBtnActionPerformed

    private void OkBtnActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_OkBtnActionPerformed
        // TODO add your handling code here:
    }//GEN-LAST:event_OkBtnActionPerformed

    private void reminderPreviewBtnActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_reminderPreviewBtnActionPerformed
        RemindNotification remind = new RemindNotification(remindNameTextField.getText(), descriptionTextArea.getText(), IconsEnum.getIconbyName((String)iconComboBox.getSelectedItem()), SoundsEnum.getSoundbyName((String)soundComboBox.getSelectedItem()));
        ReminderDialog dialog = new ReminderDialog(this, false, remind);
        dialog.setVisible(true);
    }//GEN-LAST:event_reminderPreviewBtnActionPerformed

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton OkBtn;
    private javax.swing.JCheckBox activeCheckBox;
    private javax.swing.JButton cancelBtn;
    private javax.swing.JTextArea descriptionTextArea;
    private javax.swing.JComboBox<String> iconComboBox;
    private javax.swing.JLabel jLabel1;
    private javax.swing.JScrollPane jScrollPane2;
    private remindme.customwidgets.ModernTextField remindNameTextField;
    private javax.swing.JButton reminderPreviewBtn;
    private javax.swing.JComboBox<String> soundComboBox;
    private remindme.Svg.SVGButton soundPreviewBtn;
    private remindme.Svg.SVGButton timeIntervalBtn;
    private javax.swing.JCheckBox topLevelCheckBox;
    // End of variables declaration//GEN-END:variables
}
