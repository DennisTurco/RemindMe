package remindme.Dialogs;

import java.time.LocalDateTime;

import remindme.Entities.Remind;
import remindme.Entities.RemindNotification;
import remindme.Entities.TimeInterval;
import remindme.Enums.IconsEnum;
import remindme.Enums.SoundsEnum;
import remindme.Enums.TranslationLoaderEnum.TranslationCategory;
import remindme.Enums.TranslationLoaderEnum.TranslationKey;
import remindme.Managers.RemindManager;
import remindme.Managers.SoundPlayer;

public class ManageRemind extends javax.swing.JDialog {

    private final boolean create;
    private boolean closeOk;
    private final Remind currentRemind;
    private TimeInterval timeInterval;

    public ManageRemind(java.awt.Frame parent, boolean modal, String title, String confirmBtnName, Remind remind) {
        super(parent, modal);
        this.timeInterval = remind.getTimeInterval();
        this.currentRemind = remind;
        this.create = false;

        initializeDialog(title, confirmBtnName);
        insertRemindValues(remind);
    }

    public ManageRemind(java.awt.Frame parent, boolean modal, String title, String confirmBtnName) {
        super(parent, modal);
        this.timeInterval = TimeInterval.getDefaultTimeInterval();
        this.currentRemind = null;
        this.create = true;

        initializeDialog(title, confirmBtnName);
    }

    private void initializeDialog(String title, String confirmBtnName) {
        initComponents();
        setSvgImages();
        setTitle(title);
        OkBtn.setText(confirmBtnName);
        this.closeOk = false;
        jLabel1.setText((timeInterval != null ? timeInterval.toString() : TimeInterval.getDefaultTimeInterval().toString()));

        setIcons();
        setSounds();

        if (!create)
            remindNameTextField.setEnabled(false);

        activeCheckBox.setSelected(true);
        topLevelCheckBox.setSelected(true);

        setTranslations();
    }

    public Remind getRemindInserted() {
        if (!closeOk)
            return null;

        String name = remindNameTextField.getText();
        String description = descriptionTextArea.getText();
        boolean active = activeCheckBox.isSelected();
        boolean topLevel = topLevelCheckBox.isSelected();
        IconsEnum icon = IconsEnum.getIconbyName((String) iconComboBox.getSelectedItem());
        SoundsEnum sound = SoundsEnum.getSoundbyName((String) soundComboBox.getSelectedItem());
        LocalDateTime creationDate;
        LocalDateTime lastUpdateDate;
        LocalDateTime lastExecution;
        int remindCount;
        if (create) {
            creationDate = LocalDateTime.now();
            lastUpdateDate = creationDate;
            lastExecution = null;
            remindCount = 0;
        }
        else {
            creationDate = currentRemind.getCreationDate();
            lastUpdateDate = LocalDateTime.now();
            lastExecution = currentRemind.getLastExecution();
            remindCount = currentRemind.getRemindCount();
        }

        LocalDateTime nextExecution = RemindManager.getnextExecutionByTimeInterval(timeInterval);

        //TODO: check correctness before retutn the new Remind

        return new Remind(name, description, remindCount, active, topLevel, lastExecution, nextExecution, creationDate, lastUpdateDate, timeInterval, icon, sound);
    }

    public boolean isClosedOk() {
        return closeOk;
    }

    private void updateIconImagePreview() {
        IconsEnum icon = IconsEnum.getIconbyName((String) iconComboBox.getSelectedItem());

        if (icon != null){
            iconRemindPreview.setSvgImage(icon.getIconPath(), 40, 40);
        }
    }

    private TimeInterval openTimePicker(TimeInterval time) {
        TimePicker picker = new TimePicker(this, time, true);
        picker.setVisible(true);
        return picker.getTimeInterval();
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
        remindNameTextField.setHintText(TranslationCategory.MANAGE_REMIND_DIALOG.getTranslation(TranslationKey.NAME_TEXT));
        activeCheckBox.setText(TranslationCategory.MANAGE_REMIND_DIALOG.getTranslation(TranslationKey.ACTIVE_TEXT));
        topLevelCheckBox.setText(TranslationCategory.MANAGE_REMIND_DIALOG.getTranslation(TranslationKey.TOP_LEVEL_TEXT));
        reminderPreviewBtn.setText(TranslationCategory.MANAGE_REMIND_DIALOG.getTranslation(TranslationKey.PREVIEW_TEXT));

        // remindNameTextField.setPlaceholder(TranslationCategory.MANAGE_REMIND_DIALOG.getTranslation(TranslationKey.NAME_PLACEHOLDER));
        // descriptionTextArea.setPlaceholder(TranslationCategory.MANAGE_REMIND_DIALOG.getTranslation(TranslationKey.DESCRIPTION_PLACEHOLDER));

        timeIntervalBtn.setToolTipText(TranslationCategory.TIME_PICKER_DIALOG.getTranslation(TranslationKey.TIME_INTERVAL_TITLE));
        jLabel1.setToolTipText(TranslationCategory.TIME_PICKER_DIALOG.getTranslation(TranslationKey.FORMAT));

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
        iconComboBox.addItem(IconsEnum.MEME_BABY_YODA.getIconName());
        iconComboBox.addItem(IconsEnum.MEME_DOGE.getIconName());
        iconComboBox.addItem(IconsEnum.MEME_FACEPALM.getIconName());
        iconComboBox.addItem(IconsEnum.MEME_HANDSOME_SQIDWARD.getIconName());
        iconComboBox.addItem(IconsEnum.MEME_LEONARDO_DICAPRIO.getIconName());
        iconComboBox.addItem(IconsEnum.MEME_POLITE_CAT.getIconName());
        iconComboBox.addItem(IconsEnum.MEME_ROLL_SAFE.getIconName());
        iconComboBox.addItem(IconsEnum.MEME_FINE_DOG.getIconName());
        iconComboBox.addItem(IconsEnum.MEME_LOOK_MONKEY.getIconName());
        iconComboBox.addItem(IconsEnum.MEME_OLD_MAN.getIconName());
        iconComboBox.addItem(IconsEnum.MEME_WOMAN_YELLING.getIconName());
        iconComboBox.addItem(IconsEnum.MEME_HOMER_SIMPSON.getIconName());

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
        soundComboBox.addItem(SoundsEnum.MEME_UWU.getSoundName());
        soundComboBox.addItem(SoundsEnum.MEME_BLUE_LOBSTER.getSoundName());
        soundComboBox.addItem(SoundsEnum.MEME_FUS_RO_DAH.getSoundName());
        soundComboBox.addItem(SoundsEnum.MEME_MANZ.getSoundName());
        soundComboBox.addItem(SoundsEnum.MEME_METAL_PIPE.getSoundName());
        soundComboBox.addItem(SoundsEnum.MEME_PERRO_SALCICCIA.getSoundName());
        soundComboBox.addItem(SoundsEnum.MEME_SIUM.getSoundName());
        soundComboBox.addItem(SoundsEnum.MEME_SPIN.getSoundName());
        soundComboBox.addItem(SoundsEnum.MEME_TO_BE_CONTINUED.getSoundName());

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
        iconRemindPreview = new remindme.Svg.SVGLabel();

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

        timeIntervalBtn.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                timeIntervalBtnActionPerformed(evt);
            }
        });

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
                    .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, layout.createSequentialGroup()
                        .addGap(0, 0, Short.MAX_VALUE)
                        .addComponent(OkBtn)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(cancelBtn))
                    .addComponent(remindNameTextField, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, layout.createSequentialGroup()
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                            .addComponent(iconComboBox, javax.swing.GroupLayout.Alignment.LEADING, 0, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                            .addGroup(javax.swing.GroupLayout.Alignment.LEADING, layout.createSequentialGroup()
                                .addComponent(timeIntervalBtn, javax.swing.GroupLayout.PREFERRED_SIZE, 50, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addComponent(jLabel1, javax.swing.GroupLayout.PREFERRED_SIZE, 170, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addGap(0, 0, Short.MAX_VALUE))
                            .addComponent(soundComboBox, javax.swing.GroupLayout.Alignment.LEADING, 0, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                            .addComponent(soundPreviewBtn, javax.swing.GroupLayout.PREFERRED_SIZE, 41, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(iconRemindPreview, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)))
                    .addGroup(layout.createSequentialGroup()
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING, false)
                            .addComponent(activeCheckBox, javax.swing.GroupLayout.Alignment.LEADING, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                            .addComponent(topLevelCheckBox, javax.swing.GroupLayout.Alignment.LEADING, javax.swing.GroupLayout.DEFAULT_SIZE, 393, Short.MAX_VALUE))
                        .addGap(0, 0, Short.MAX_VALUE)))
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
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                    .addComponent(iconComboBox, javax.swing.GroupLayout.DEFAULT_SIZE, 41, Short.MAX_VALUE)
                    .addComponent(iconRemindPreview, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
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
        updateIconImagePreview();
    }//GEN-LAST:event_iconComboBoxActionPerformed

    private void cancelBtnActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_cancelBtnActionPerformed
        closeOk = false;
        this.dispose();
    }//GEN-LAST:event_cancelBtnActionPerformed

    private void OkBtnActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_OkBtnActionPerformed
        closeOk = true;
        this.dispose();

    }//GEN-LAST:event_OkBtnActionPerformed

    private void reminderPreviewBtnActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_reminderPreviewBtnActionPerformed
        RemindNotification remind = new RemindNotification(remindNameTextField.getText(), descriptionTextArea.getText(), IconsEnum.getIconbyName((String)iconComboBox.getSelectedItem()), SoundsEnum.getSoundbyName((String)soundComboBox.getSelectedItem()), topLevelCheckBox.isSelected());
        ReminderDialog dialog = new ReminderDialog(this, false, remind, true);
        dialog.setVisible(true);
    }//GEN-LAST:event_reminderPreviewBtnActionPerformed

    private void timeIntervalBtnActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_timeIntervalBtnActionPerformed
        TimeInterval newTimeInterval = openTimePicker(timeInterval);

        if (newTimeInterval == null) {
            return;
        }

        timeInterval = newTimeInterval;
        jLabel1.setText(timeInterval.toString());
    }//GEN-LAST:event_timeIntervalBtnActionPerformed

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton OkBtn;
    private javax.swing.JCheckBox activeCheckBox;
    private javax.swing.JButton cancelBtn;
    private javax.swing.JTextArea descriptionTextArea;
    private javax.swing.JComboBox<String> iconComboBox;
    private remindme.Svg.SVGLabel iconRemindPreview;
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
