package seedu.address.logic.commands;

import static java.util.Objects.requireNonNull;
import static seedu.address.logic.parser.CliSyntax.PREFIX_ADDRESS;
import static seedu.address.logic.parser.CliSyntax.PREFIX_APPT_DATE;
import static seedu.address.logic.parser.CliSyntax.PREFIX_CONDITION;
import static seedu.address.logic.parser.CliSyntax.PREFIX_DETAILS;
import static seedu.address.logic.parser.CliSyntax.PREFIX_GENDER;
import static seedu.address.logic.parser.CliSyntax.PREFIX_MEDICINE;
import static seedu.address.logic.parser.CliSyntax.PREFIX_NAME;
import static seedu.address.logic.parser.CliSyntax.PREFIX_PHONE;
import static seedu.address.model.Model.PREDICATE_SHOW_ALL_PERSONS;

import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;

import seedu.address.commons.core.index.Index;
import seedu.address.commons.util.CollectionUtil;
import seedu.address.commons.util.ToStringBuilder;
import seedu.address.logic.Messages;
import seedu.address.logic.commands.exceptions.CommandException;
import seedu.address.model.Model;
import seedu.address.model.person.Address;
import seedu.address.model.person.AppointmentDate;
import seedu.address.model.person.Gender;
import seedu.address.model.person.Medicine;
import seedu.address.model.person.Name;
import seedu.address.model.person.Person;
import seedu.address.model.person.Phone;
import seedu.address.model.tag.Tag;

/**
 * Edits the details of an existing person in the address book.
 */
public class EditCommand extends Command {

    public static final String COMMAND_WORD = "edit";

    public static final String MESSAGE_USAGE = COMMAND_WORD + ": Edits the details of the person identified "
            + "by the index number used in the displayed person list. "
            + "Existing values will be overwritten by the input values.\n"
            + "Parameters: INDEX (must be a positive integer) "
            + "[" + PREFIX_NAME + "NAME] "
            + "[" + PREFIX_PHONE + "PHONE] "
            + "[" + PREFIX_ADDRESS + "ADDRESS] "
            + "[" + PREFIX_GENDER + "GENDER] "
            + "[" + PREFIX_APPT_DATE + "APPOINTMENT DATE] "
            + "[" + PREFIX_CONDITION + " CONDITION]... "
            + "[" + PREFIX_DETAILS + " DETAILS]... "
            + "[" + PREFIX_MEDICINE + " MEDICINE]" + "\n"
            + "Example: " + COMMAND_WORD + " 1 "
            + PREFIX_NAME + "John Doe "
            + PREFIX_PHONE + "91234567 "
            + PREFIX_GENDER + "male "
            + PREFIX_ADDRESS + "Block 123 Clementi Street S1234567 "
            + PREFIX_APPT_DATE + "2020-02-02 "
            + PREFIX_CONDITION + " High BP "
            + PREFIX_DETAILS + " Stays alone "
            + PREFIX_MEDICINE + " paracetamol ";

    public static final String MESSAGE_EDIT_PERSON_SUCCESS = "Edited Person: %1$s";
    public static final String MESSAGE_NOT_EDITED = "At least one field to edit must be provided.";
    public static final String MESSAGE_DUPLICATE_PERSON = "This person already exists in the address book.";

    private final Index index;
    private final EditPersonDescriptor editPersonDescriptor;

    /**
     * @param index of the person in the filtered person list to edit
     * @param editPersonDescriptor details to edit the person with
     */
    public EditCommand(Index index, EditPersonDescriptor editPersonDescriptor) {
        requireNonNull(index);
        requireNonNull(editPersonDescriptor);

        this.index = index;
        this.editPersonDescriptor = new EditPersonDescriptor(editPersonDescriptor);
    }

    @Override
    public CommandResult execute(Model model) throws CommandException {
        requireNonNull(model);

        List<Person> lastShownList = model.getFilteredPersonList();

        if (index.getZeroBased() >= lastShownList.size()) {
            throw new CommandException(Messages.MESSAGE_INVALID_PERSON_DISPLAYED_INDEX);
        }

        Person personToEdit = lastShownList.get(index.getZeroBased());
        Person editedPerson = createEditedPerson(personToEdit, editPersonDescriptor);

        if (!personToEdit.isSamePerson(editedPerson)
                && model.getAddressBook().getPersonList().stream()
                .anyMatch(p -> !p.equals(personToEdit) && p.isSamePerson(editedPerson))) {
            throw new CommandException(MESSAGE_DUPLICATE_PERSON);
        }

        model.setPerson(personToEdit, editedPerson);
        model.updateFilteredPersonList(PREDICATE_SHOW_ALL_PERSONS);
        return new CommandResult(String.format(MESSAGE_EDIT_PERSON_SUCCESS, Messages.format(editedPerson)));
    }

    /**
     * Creates and returns a {@code Person} with the details of {@code personToEdit}
     * edited with {@code editPersonDescriptor}.
     */
    private static Person createEditedPerson(Person personToEdit, EditPersonDescriptor editPersonDescriptor) {
        assert personToEdit != null;

        Name updatedName = editPersonDescriptor.getName().orElse(personToEdit.getName());
        Phone updatedPhone = editPersonDescriptor.getPhone().orElse(personToEdit.getPhone());
        Address updatedAddress = editPersonDescriptor.getAddress().orElse(personToEdit.getAddress());
        Gender updatedGender = editPersonDescriptor.getGender().orElse(personToEdit.getGender());
        AppointmentDate updatedAppointmentDate = editPersonDescriptor.getAppointmentDate()
                .orElse(personToEdit.getAppointmentDate());
        Medicine updatedMedicine = editPersonDescriptor.getMedicine().orElse(personToEdit.getMedicine());
        Set<Tag> updatedConditionTags = editPersonDescriptor.getConditionTags().orElse(personToEdit.getConditionTags());
        Set<Tag> updatedDetailTags = editPersonDescriptor.getDetailTags().orElse(personToEdit.getDetailTags());

        return new Person(updatedName, updatedPhone, updatedAddress,
                updatedGender, updatedAppointmentDate, updatedMedicine, updatedConditionTags, updatedDetailTags);
    }

    @Override
    public boolean equals(Object other) {
        if (other == this) {
            return true;
        }

        // instanceof handles nulls
        if (!(other instanceof EditCommand)) {
            return false;
        }

        EditCommand otherEditCommand = (EditCommand) other;
        return index.equals(otherEditCommand.index)
                && editPersonDescriptor.equals(otherEditCommand.editPersonDescriptor);
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this)
                .add("index", index)
                .add("editPersonDescriptor", editPersonDescriptor)
                .toString();
    }

    /**
     * Stores the details to edit the person with. Each non-empty field value will replace the
     * corresponding field value of the person.
     */
    public static class EditPersonDescriptor {
        private Name name;
        private Phone phone;
        private Address address;
        private Gender gender;
        private AppointmentDate appointmentDate;
        private Set<Tag> conditionTags;
        private Set<Tag> detailTags;
        private Medicine medicine;

        public EditPersonDescriptor() {}

        /**
         * Copy constructor.
         * A defensive copy of {@code tags} is used internally.
         */
        public EditPersonDescriptor(EditPersonDescriptor toCopy) {
            setName(toCopy.name);
            setPhone(toCopy.phone);
            setAddress(toCopy.address);
            setGender(toCopy.gender);
            setAppointmentDate(toCopy.appointmentDate);
            setConditionTags(toCopy.conditionTags);
            setDetailTags(toCopy.detailTags);
            setMedicine(toCopy.medicine);
        }

        /**
         * Returns true if at least one field is edited.
         */
        public boolean isAnyFieldEdited() {
            return CollectionUtil.isAnyNonNull(name, phone, address, gender, appointmentDate, conditionTags,
                    detailTags, medicine);
        }

        public void setName(Name name) {
            this.name = name;
        }

        public Optional<Name> getName() {
            return Optional.ofNullable(name);
        }

        public void setPhone(Phone phone) {
            this.phone = phone;
        }

        public Optional<Phone> getPhone() {
            return Optional.ofNullable(phone);
        }

        public void setGender(Gender gender) {
            this.gender = gender;
        }

        public Optional<Gender> getGender() {
            return Optional.ofNullable(gender);
        }

        public void setAddress(Address address) {
            this.address = address;
        }

        public Optional<Address> getAddress() {
            return Optional.ofNullable(address);
        }

        public void setAppointmentDate(AppointmentDate appointmentDate) {
            this.appointmentDate = appointmentDate;
        }

        public Optional<AppointmentDate> getAppointmentDate() {
            return Optional.ofNullable(appointmentDate);
        }

        public void setMedicine(Medicine medicine) {
            this.medicine = medicine;
        }

        public Optional<Medicine> getMedicine() {
            return Optional.ofNullable(medicine);
        }

        /**
         * Sets {@code tags} to this object's {@code conditionTags}.
         * A defensive copy of {@code tags} is used internally.
         */
        public void setConditionTags(Set<Tag> tags) {
            this.conditionTags = (tags != null) ? new HashSet<>(tags) : null;
        }

        /**
         * Sets {@code tags} to this object's {@code detailTags}.
         * A defensive copy of {@code tags} is used internally.
         */
        public void setDetailTags(Set<Tag> tags) {
            this.detailTags = (tags != null) ? new HashSet<>(tags) : null;
        }

        /**
         * Returns an unmodifiable tag set, which throws {@code UnsupportedOperationException}
         * if modification is attempted.
         * Returns {@code Optional#empty()} if {@code conditionTags} is null.
         */
        public Optional<Set<Tag>> getConditionTags() {
            return (conditionTags != null) ? Optional.of(Collections.unmodifiableSet(conditionTags)) : Optional.empty();
        }

        /**
         * Returns an unmodifiable tag set, which throws {@code UnsupportedOperationException}
         * if modification is attempted.
         * Returns {@code Optional#empty()} if {@code detailTags} is null.
         */
        public Optional<Set<Tag>> getDetailTags() {
            return (detailTags != null) ? Optional.of(Collections.unmodifiableSet(detailTags)) : Optional.empty();
        }

        @Override
        public boolean equals(Object other) {
            if (other == this) {
                return true;
            }

            // instanceof handles nulls
            if (!(other instanceof EditPersonDescriptor)) {
                return false;
            }

            EditPersonDescriptor otherEditPersonDescriptor = (EditPersonDescriptor) other;
            return Objects.equals(name, otherEditPersonDescriptor.name)
                    && Objects.equals(phone, otherEditPersonDescriptor.phone)
                    && Objects.equals(address, otherEditPersonDescriptor.address)
                    && Objects.equals(gender, otherEditPersonDescriptor.gender)
                    && Objects.equals(appointmentDate, otherEditPersonDescriptor.appointmentDate)
                    && Objects.equals(conditionTags, otherEditPersonDescriptor.conditionTags)
                    && Objects.equals(detailTags, otherEditPersonDescriptor.detailTags)
                    && Objects.equals(medicine, otherEditPersonDescriptor.medicine);
        }

        @Override
        public String toString() {
            return new ToStringBuilder(this)
                    .add("name", name)
                    .add("phone", phone)
                    .add("address", address)
                    .add("gender", gender)
                    .add("appointment date", appointmentDate)
                    .add("conditionTags", conditionTags)
                    .add("detailTags", detailTags)
                    .add("medicine", medicine)
                    .toString();
        }
    }
}
