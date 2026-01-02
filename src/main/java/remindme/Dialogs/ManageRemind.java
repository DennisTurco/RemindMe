package remindme.Dialogs;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;

import remindme.Entities.Remind;
import remindme.Entities.RemindNotification;
import remindme.Entities.TimeInterval;
import remindme.Enums.ExecutionMethod;
import remindme.Enums.IconsEnum;
import remindme.Enums.SoundsEnum;
import remindme.Enums.TranslationLoaderEnum.TranslationCategory;
import remindme.Enums.TranslationLoaderEnum.TranslationKey;
import remindme.Helpers.TimeRange;
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
        timeFrequencyLabel.setText((timeInterval != null ? timeInterval.toString() : TimeInterval.getDefaultTimeInterval().toString()));

        setIcons();
        setSounds();
        setExecutionMethods();

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
        ExecutionMethod executionMethod = ExecutionMethod.getExecutionMethodbyName((String) executionMethodComboBox.getSelectedItem());
        LocalDateTime creationDate, lastUpdateDate, lastExecution;
        LocalTime timeFromLocalTime = executionMethod == ExecutionMethod.PC_STARTUP ? null : timeFrom.getTime();
        LocalTime timeToLocalTime = executionMethod == ExecutionMethod.PC_STARTUP ? null : timeTo.getTime();

        int remindCount, maxExecutionsPerDay = 0;
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

        LocalDateTime nextExecution = RemindManager.getNextExecutionBasedOnMethod(executionMethod, timeFromLocalTime, timeToLocalTime, timeInterval);

        return new Remind(name, description, remindCount, active, topLevel, lastExecution, nextExecution, creationDate, lastUpdateDate, timeInterval, icon, sound, executionMethod, timeFromLocalTime, timeToLocalTime, maxExecutionsPerDay);
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

    public boolean isTimeRangeValid() {
        if (ExecutionMethod.getExecutionMethodbyName(executionMethodComboBox.getSelectedItem().toString()) == ExecutionMethod.PC_STARTUP) {
            return true;
        }

        if (ExecutionMethod.getExecutionMethodbyName(executionMethodComboBox.getSelectedItem().toString()) == ExecutionMethod.ONE_TIME_PER_DAY) {
            return true;
        }

        isTimeRangeValid(timeFrom.getTime(), timeTo.getTime());

        return true;
    }

    public static boolean isTimeRangeValid(LocalTime timeFrom, LocalTime timeTo) {
        try {
            // if the object creeation fails it means that the times
            TimeRange.of(timeFrom, timeTo);
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    private void insertRemindValues(Remind remind) {
        remindNameTextField.setText(remind.getName());
        descriptionTextArea.setText(remind.getDescription());
        activeCheckBox.setSelected(remind.isActive());
        topLevelCheckBox.setSelected(remind.isTopLevel());
        iconComboBox.setSelectedItem(remind.getIcon().getIconName());
        soundComboBox.setSelectedItem(remind.getSound().getSoundName());
        executionMethodComboBox.setSelectedItem(remind.getExecutionMethod().getExecutionMethodName());
        timeFrom.setText(remind.getTimeFrom() != null ? remind.getTimeFrom().toString() : "");
        timeTo.setText(remind.getTimeTo() != null ? remind.getTimeTo().toString(): "");
    }

    private void setSvgImages() {
        soundPreviewBtn.setSvgImage("res/img/sound.svg", 35, 35);
        timeIntervalBtn.setSvgImage("res/img/timer.svg", 50, 50);
    }

    private void setTranslations() {
        remindNameTextField.setHintText(TranslationCategory.MANAGE_REMIND_DIALOG.getTranslation(TranslationKey.NAME_TEXT));
        activeCheckBox.setText(TranslationCategory.MANAGE_REMIND_DIALOG.getTranslation(TranslationKey.ACTIVE_TEXT));
        topLevelCheckBox.setText(TranslationCategory.MANAGE_REMIND_DIALOG.getTranslation(TranslationKey.TOP_LEVEL_TEXT));
        reminderPreviewBtn.setText(TranslationCategory.MANAGE_REMIND_DIALOG.getTranslation(TranslationKey.PREVIEW_TEXT));
        fromLabel.setText(TranslationCategory.MANAGE_REMIND_DIALOG.getTranslation(TranslationKey.DATE_FROM_TEXT) + ":");
        toLabel.setText(TranslationCategory.MANAGE_REMIND_DIALOG.getTranslation(TranslationKey.DATE_TO_TEXT) + ":");

        timeIntervalBtn.setToolTipText(TranslationCategory.TIME_PICKER_DIALOG.getTranslation(TranslationKey.TIME_INTERVAL_TITLE));
        timeFrequencyLabel.setToolTipText(TranslationCategory.TIME_PICKER_DIALOG.getTranslation(TranslationKey.FORMAT));

        remindNameTextField.setToolTipText(TranslationCategory.MANAGE_REMIND_DIALOG.getTranslation(TranslationKey.NAME_TOOLTIP));
        descriptionTextArea.setToolTipText(TranslationCategory.MANAGE_REMIND_DIALOG.getTranslation(TranslationKey.DESCRIPTION_TOOLTIP));
        activeCheckBox.setToolTipText(TranslationCategory.MANAGE_REMIND_DIALOG.getTranslation(TranslationKey.ACTIVE_TOOLTIP));
        topLevelCheckBox.setToolTipText(TranslationCategory.MANAGE_REMIND_DIALOG.getTranslation(TranslationKey.TOP_LEVEL_TOOLTIP));
        iconComboBox.setToolTipText(TranslationCategory.MANAGE_REMIND_DIALOG.getTranslation(TranslationKey.ICON_TOOLTIP));
        soundComboBox.setToolTipText(TranslationCategory.MANAGE_REMIND_DIALOG.getTranslation(TranslationKey.SOUND_TOOLTIP));
        soundPreviewBtn.setToolTipText(TranslationCategory.MANAGE_REMIND_DIALOG.getTranslation(TranslationKey.SOUND_BUTTON_TOOLTIP));
        executionMethodComboBox.setToolTipText(TranslationCategory.MANAGE_REMIND_DIALOG.getTranslation(TranslationKey.EXECUTION_METHOD_TOOLTIP));
    }

    private void enableBasedOnExecutionMethod(boolean fromEnable, boolean toEnable) {
        fromLabel.setEnabled(fromEnable);
        timeFrom.setEnabled(fromEnable);

        toLabel.setEnabled(toEnable);
        timeTo.setEnabled(toEnable);
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

        soundComboBox.addItem(SoundsEnum.NO_SOUND.getSoundName());
        soundComboBox.addItem(SoundsEnum.SOUND1.getSoundName());
        soundComboBox.addItem(SoundsEnum.SOUND2.getSoundName());
        soundComboBox.addItem(SoundsEnum.SOUND3.getSoundName());
        soundComboBox.addItem(SoundsEnum.SOUND4.getSoundName());
        soundComboBox.addItem(SoundsEnum.SOUND5.getSoundName());
        // soundComboBox.addItem(SoundsEnum.SOUND6.getSoundName());
        // soundComboBox.addItem(SoundsEnum.SOUND7.getSoundName());
        soundComboBox.addItem(SoundsEnum.SOUND8.getSoundName());
        soundComboBox.addItem(SoundsEnum.SOUND9.getSoundName());
        // soundComboBox.addItem(SoundsEnum.SOUND10.getSoundName());
        soundComboBox.addItem(SoundsEnum.SOUND11.getSoundName());
        soundComboBox.addItem(SoundsEnum.SOUND12.getSoundName());
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

    private void setExecutionMethods() {
        executionMethodComboBox.removeAllItems();

        executionMethodComboBox.addItem(ExecutionMethod.PC_STARTUP.getExecutionMethodName());
        executionMethodComboBox.addItem(ExecutionMethod.CUSTOM_TIME_RANGE.getExecutionMethodName());
        executionMethodComboBox.addItem(ExecutionMethod.ONE_TIME_PER_DAY.getExecutionMethodName());

        executionMethodComboBox.setSelectedItem(ExecutionMethod.getDefaultExecutionMethod());
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
        timeFrequencyLabel = new javax.swing.JLabel();
        timeIntervalBtn = new remindme.Svg.SVGButton();
        soundPreviewBtn = new remindme.Svg.SVGButton();
        iconRemindPreview = new remindme.Svg.SVGLabel();
        executionMethodComboBox = new javax.swing.JComboBox<>();
        timeFrom = new com.github.lgooddatepicker.components.TimePicker();
        timeTo = new com.github.lgooddatepicker.components.TimePicker();
        jSeparator1 = new javax.swing.JSeparator();
        jSeparator2 = new javax.swing.JSeparator();
        toLabel = new javax.swing.JLabel();
        fromLabel = new javax.swing.JLabel();

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

        timeFrequencyLabel.setText("timeFrequencyLabel");

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

        executionMethodComboBox.setModel(new javax.swing.DefaultComboBoxModel<>(new String[] { "Item 1", "Item 2", "Item 3", "Item 4" }));
        executionMethodComboBox.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                executionMethodComboBoxActionPerformed(evt);
            }
        });

        timeFrom.setFocusable(false);
        timeFrom.setRequestFocusEnabled(false);

        timeTo.setFocusable(false);
        timeTo.setRequestFocusEnabled(false);

        toLabel.setHorizontalAlignment(javax.swing.SwingConstants.RIGHT);
        toLabel.setText("To:");

        fromLabel.setHorizontalAlignment(javax.swing.SwingConstants.RIGHT);
        fromLabel.setText("From:");
        fromLabel.setToolTipText("");

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(getContentPane());
        getContentPane().setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jSeparator1, javax.swing.GroupLayout.Alignment.TRAILING)
                    .addComponent(jScrollPane2, javax.swing.GroupLayout.Alignment.TRAILING)
                    .addComponent(remindNameTextField, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, layout.createSequentialGroup()
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                            .addComponent(iconComboBox, javax.swing.GroupLayout.Alignment.LEADING, 0, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                            .addGroup(javax.swing.GroupLayout.Alignment.LEADING, layout.createSequentialGroup()
                                .addComponent(timeIntervalBtn, javax.swing.GroupLayout.PREFERRED_SIZE, 50, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                    .addGroup(layout.createSequentialGroup()
                                        .addComponent(timeFrequencyLabel, javax.swing.GroupLayout.PREFERRED_SIZE, 170, javax.swing.GroupLayout.PREFERRED_SIZE)
                                        .addGap(114, 114, 114))
                                    .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, layout.createSequentialGroup()
                                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, 58, javax.swing.GroupLayout.PREFERRED_SIZE)
                                        .addComponent(reminderPreviewBtn, javax.swing.GroupLayout.PREFERRED_SIZE, 226, javax.swing.GroupLayout.PREFERRED_SIZE)))
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
                        .addGap(0, 0, Short.MAX_VALUE))
                    .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, layout.createSequentialGroup()
                        .addGap(0, 0, Short.MAX_VALUE)
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                            .addGroup(layout.createSequentialGroup()
                                .addComponent(OkBtn)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addComponent(cancelBtn))
                            .addGroup(layout.createSequentialGroup()
                                .addComponent(executionMethodComboBox, javax.swing.GroupLayout.PREFERRED_SIZE, 160, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addComponent(fromLabel, javax.swing.GroupLayout.PREFERRED_SIZE, 61, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addComponent(timeFrom, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addComponent(toLabel, javax.swing.GroupLayout.PREFERRED_SIZE, 61, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addComponent(timeTo, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))))
                    .addComponent(jSeparator2, javax.swing.GroupLayout.Alignment.TRAILING))
                .addContainerGap())
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
                .addComponent(jSeparator1, javax.swing.GroupLayout.PREFERRED_SIZE, 10, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                    .addComponent(executionMethodComboBox, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                        .addComponent(timeFrom, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                        .addComponent(toLabel, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                        .addComponent(fromLabel, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                        .addComponent(timeTo, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(timeFrequencyLabel, javax.swing.GroupLayout.PREFERRED_SIZE, 50, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(timeIntervalBtn, javax.swing.GroupLayout.PREFERRED_SIZE, 50, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addComponent(jSeparator2, javax.swing.GroupLayout.PREFERRED_SIZE, 10, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(18, 18, 18)
                .addComponent(reminderPreviewBtn)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
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
        timeFrequencyLabel.setText(timeInterval.toString());
    }//GEN-LAST:event_timeIntervalBtnActionPerformed

    private void executionMethodComboBoxActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_executionMethodComboBoxActionPerformed
        if (executionMethodComboBox.getSelectedItem() == null)
            return;

        boolean customTimeEnable = executionMethodComboBox.getSelectedItem().equals(ExecutionMethod.CUSTOM_TIME_RANGE.getExecutionMethodName());
        boolean oneTimePerDayEnable = executionMethodComboBox.getSelectedItem().equals(ExecutionMethod.ONE_TIME_PER_DAY.getExecutionMethodName());
        boolean pcStartupEnable = executionMethodComboBox.getSelectedItem().equals(ExecutionMethod.PC_STARTUP.getExecutionMethodName());

        if (customTimeEnable) {
            enableBasedOnExecutionMethod(true, true);
        }
        else if (oneTimePerDayEnable) {
            enableBasedOnExecutionMethod(true, false);
            timeIntervalBtn.setEnabled(false);
            return;
        }
        else if (pcStartupEnable) {
            enableBasedOnExecutionMethod(false, false);
        }
        timeIntervalBtn.setEnabled(true);
    }//GEN-LAST:event_executionMethodComboBoxActionPerformed

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton OkBtn;
    private javax.swing.JCheckBox activeCheckBox;
    private javax.swing.JButton cancelBtn;
    private javax.swing.JTextArea descriptionTextArea;
    private javax.swing.JComboBox<String> executionMethodComboBox;
    private javax.swing.JLabel fromLabel;
    private javax.swing.JComboBox<String> iconComboBox;
    private remindme.Svg.SVGLabel iconRemindPreview;
    private javax.swing.JScrollPane jScrollPane2;
    private javax.swing.JSeparator jSeparator1;
    private javax.swing.JSeparator jSeparator2;
    private remindme.customwidgets.ModernTextField remindNameTextField;
    private javax.swing.JButton reminderPreviewBtn;
    private javax.swing.JComboBox<String> soundComboBox;
    private remindme.Svg.SVGButton soundPreviewBtn;
    private javax.swing.JLabel timeFrequencyLabel;
    private com.github.lgooddatepicker.components.TimePicker timeFrom;
    private remindme.Svg.SVGButton timeIntervalBtn;
    private com.github.lgooddatepicker.components.TimePicker timeTo;
    private javax.swing.JLabel toLabel;
    private javax.swing.JCheckBox topLevelCheckBox;
    // End of variables declaration//GEN-END:variables
}
