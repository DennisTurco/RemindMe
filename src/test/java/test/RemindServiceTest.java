package test;

import java.io.File;
import java.io.IOException;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import remindme.Entities.Preferences;
import remindme.Entities.Remind;
import remindme.Entities.RemindListPath;
import remindme.Entities.TimeInterval;
import remindme.Enums.ExecutionMethod;
import remindme.Enums.LanguagesEnum;
import remindme.Enums.ThemesEnum;
import remindme.Helpers.TimeRange;
import remindme.Services.RemindService;

class RemindServiceTest {

    private RemindService remindService;

    private File tempRemindFile;

    @BeforeEach
    protected void setUp() throws IOException {
        buildPreferenceFile();

        // Reset static state before each test
        RemindService.setReminds(new ArrayList<>());

        remindService = new RemindService();
    }

    private Remind createSampleRemind(String name) {
        return new Remind(
            name,
            "description",
            0,
            true,
            false,
            null,
            LocalDateTime.now().plusMinutes(10),
            LocalDateTime.now(),
            LocalDateTime.now(),
            new TimeInterval(0, 0, 10),
            null,
            null,
            ExecutionMethod.PC_STARTUP,
            new TimeRange(LocalTime.now(), LocalTime.now()),
            0
        );
    }

    @Test
    void shouldAddRemind() {
        Remind remind = createSampleRemind("Test");

        remindService.addRemindAndReload(remind);

        assertEquals(1, RemindService.getReminds().size());
        assertEquals("Test", RemindService.getReminds().get(0).getName());
    }

    @Test
    void shouldDetectDuplicatedName() {
        Remind r1 = createSampleRemind("Duplicate");
        Remind r2 = createSampleRemind("Duplicate");

        RemindService.setReminds(List.of(r1));

        assertTrue(remindService.isRemindNameDuplicated(r2.getName()));
    }

    @Test
    void shouldRenameRemind() {
        Remind remind = createSampleRemind("OldName");
        RemindService.setReminds(new ArrayList<>(List.of(remind)));

        remindService.renameRemind(remind, "NewName");

        assertEquals("NewName", RemindService.getReminds().get(0).getName());
    }

    @Test
    void shouldRemoveRemindByIndex() {
        Remind r1 = createSampleRemind("A");
        Remind r2 = createSampleRemind("B");

        RemindService.setReminds(new ArrayList<>(List.of(r1, r2)));

        remindService.removeReminder(0, false);

        assertEquals(1, RemindService.getReminds().size());
        assertEquals("B", RemindService.getReminds().get(0).getName());
    }

    @Test
    void shouldDuplicateRemindWithNewName() {
        Remind remind = createSampleRemind("Base");
        RemindService.setReminds(new ArrayList<>(List.of(remind)));

        remindService.duplicateReminder(remind);

        assertEquals(2, RemindService.getReminds().size());
        assertNotEquals(
            RemindService.getReminds().get(0).getName(),
            RemindService.getReminds().get(1).getName()
        );
    }

    @Test
    void shouldFilterRemindsByName() {
        Remind r1 = createSampleRemind("Meeting");
        Remind r2 = createSampleRemind("Shopping");

        RemindService.setReminds(new ArrayList<>(List.of(r1, r2)));

        List<Remind> result = remindService.getSubListWithFilterResearchByString("meet");

        assertEquals(1, result.size());
        assertEquals("Meeting", result.get(0).getName());
    }

    private void buildPreferenceFile() throws IOException {
        buildTempFile();
        buildPreferencesObject();
    }

    private void buildPreferencesObject() {
        Preferences.setLanguage(LanguagesEnum.ENG);
        Preferences.setTheme(ThemesEnum.INTELLIJ.getThemeName());

        Preferences.setRemindList(
            new RemindListPath(
                tempRemindFile.getParent() + File.separator,
                tempRemindFile.getName()
            )
        );
    }

    private void buildTempFile() throws IOException {
        tempRemindFile = File.createTempFile("remind-test-", ".json");
        tempRemindFile.deleteOnExit();
    }
}
