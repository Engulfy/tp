---
layout: page
title: Developer Guide
---
* Table of Contents
{:toc}

--------------------------------------------------------------------------------------------------------------------

## **Acknowledgements**

We would like to humbly acknowledge the following for the success of our project:
* The SE-EDU team for creating and maintaining the AddressBook-Level3 project.
* Our course instructors and teaching assistants whose patient guidance and feedback were instrumental throughout the development journey.
* Our peers and collaborators for their support, constructive code reviews, and insightful discussions. 
* The open-source Java and JavaFX communities for providing comprehensive documentation and development tools.
* The developers and maintainers of essential libraries and frameworks used in this project, including Jackson for JSON processing and JUnit for testing.

### **Java Dependencies**

* **JavaFX** - for Graphical User Interface (GUI) rendering 
* **Jackson** - for JSON serialization and deserialization
* **JUnit 5** - for JUnit testing
* **JaCoCo** - for generating test coverage reports 
* **Gradle Shadow Plugin** - for creating executable JAR files with dependencies
* **Checkstyle** - for enforcing coding standards 

### **Documentation Tools**

* **Jeckyll** - for authoring and building the project website
* **PlantUML** - for creating UML diagrams used in the Development Guide

### **Badges**

* **CodeCov** - for providing code coverage badge
* **GitHub Actions** - for providing the Java CI badge
--------------------------------------------------------------------------------------------------------------------

## **Setting up, getting started**

Refer to the guide [_Setting up and getting started_](SettingUp.md).

--------------------------------------------------------------------------------------------------------------------

## **Design**

<div markdown="span" class="alert alert-primary">

:bulb: **Tip:** The `.puml` files used to create diagrams in this document `docs/diagrams` folder. Refer to the [_PlantUML Tutorial_ at se-edu/guides](https://se-education.org/guides/tutorials/plantUml.html) to learn how to create and edit diagrams.
</div>

### Architecture

<img src="images/ArchitectureDiagram.png" width="280" />

The ***Architecture Diagram*** given above explains the high-level design of the App.

Given below is a quick overview of main components and how they interact with each other.

**Main components of the architecture**

**`Main`** (consisting of classes [`Main`](https://github.com/AY2425S2-CS2103T-T12-4/tp/blob/master/src/main/java/seedu/address/Main.java) and [`MainApp`](https://github.com/AY2425S2-CS2103T-T12-4/tp/blob/master/src/main/java/seedu/address/MainApp.java)) is in charge of the app launch and shut down.
* At app launch, it initializes the other components in the correct sequence, and connects them up with each other.
* At shut down, it shuts down the other components and invokes cleanup methods where necessary.

The bulk of the app's work is done by the following four components:

* [**`UI`**](#ui-component): The UI of the App.
* [**`Logic`**](#logic-component): The command executor.
* [**`Model`**](#model-component): Holds the data of the App in memory.
* [**`Storage`**](#storage-component): Reads data from, and writes data to, the hard disk.

[**`Commons`**](#common-classes) represents a collection of classes used by multiple other components.

**How the architecture components interact with each other**

The *Sequence Diagram* below shows how the components interact with each other for the scenario where the user issues the command `delete 1`.

<img src="images/ArchitectureSequenceDiagram.png" width="574" />

Each of the four main components (also shown in the diagram above),

* defines its *API* in an `interface` with the same name as the Component.
* implements its functionality using a concrete `{Component Name}Manager` class (which follows the corresponding API `interface` mentioned in the previous point.

For example, the `Logic` component defines its API in the `Logic.java` interface and implements its functionality using the `LogicManager.java` class which follows the `Logic` interface. Other components interact with a given component through its interface rather than the concrete class (reason: to prevent outside component's being coupled to the implementation of a component), as illustrated in the (partial) class diagram below.

<img src="images/ComponentManagers.png" width="300" />

The sections below give more details of each component.

### UI component

The **API** of this component is specified in [`Ui.java`](https://github.com/AY2425S2-CS2103T-T12-4/tp/blob/master/src/main/java/seedu/address/ui/Ui.java)

![Structure of the UI Component](images/UiClassDiagram.png)

The UI consists of a `MainWindow` that is made up of parts e.g.`CommandBox`, `ResultDisplay`, `PersonListPanel`, `StatusBarFooter` etc. All these, including the `MainWindow`, inherit from the abstract `UiPart` class which captures the commonalities between classes that represent parts of the visible GUI.

The `UI` component uses the JavaFx UI framework. The layout of these UI parts are defined in matching `.fxml` files that are in the `src/main/resources/view` folder. For example, the layout of the [`MainWindow`](https://github.com/AY2425S2-CS2103T-T12-4/tp/blob/master/src/main/java/seedu/address/ui/MainWindow.java) is specified in [`MainWindow.fxml`](https://github.com/AY2425S2-CS2103T-T12-4/tp/blob/master/src/main/resources/view/MainWindow.fxml)

The `UI` component,

* executes user commands using the `Logic` component.
* listens for changes to `Model` data so that the UI can be updated with the modified data.
* keeps a reference to the `Logic` component, because the `UI` relies on the `Logic` to execute commands.
* depends on some classes in the `Model` component, as it displays `Person` object residing in the `Model`.

### Logic component

**API** : [`Logic.java`](https://github.com/AY2425S2-CS2103T-T12-4/tp/blob/master/src/main/java/seedu/address/logic/Logic.java)

Here's a (partial) class diagram of the `Logic` component:

<img src="images/LogicClassDiagram.png" width="550"/>

The sequence diagram below illustrates the interactions within the `Logic` component, taking execution of delete command API call as an example.

![Interactions Inside the Logic Component for the `delete 1` Command](images/DeleteSequenceDiagram.png)

<div markdown="span" class="alert alert-info">:information_source: **Note:** The lifeline for `DeleteCommandParser` should end at the destroy marker (X) but due to a limitation of PlantUML, the lifeline continues till the end of diagram.
</div>

How the `Logic` component works:

1. When `LogicManager` is called upon to execute a command (e.g. `delete 1`), it passes the input to the `AddressBookParser`.

2. The `AddressBookParser` identifies the command and, if necessary, creates a specific parser (e.g. `DeleteCommandParser`) to parse the input.

3. This results in a `Command` object (e.g. a `DeleteCommand`) which is returned and then executed by `LogicManager`.

4. The command interacts with the `Model` during execution — for example, it may call `setPendingDeletion(person)` to store the target person while waiting for user confirmation.

5. The result of this execution is wrapped in a `CommandResult` object, which is returned from `Logic`.

---

6. When the user types `y` to confirm, the input is passed again to `LogicManager`, which treats it as a **new command**.

7. The `AddressBookParser` parses this as a confirmation and directly returns a `ConfirmCommand`.
   > 📌 *Note:* `AddressBookParser` does **not** create a specific parser for confirm commands, as they are simple and handled directly.

8. `LogicManager` executes the `ConfirmCommand`, which:
    - Retrieves the person marked for deletion via `getPendingDeletion()`
    - Performs the deletion using `deletePerson(person)`

9. A new `CommandResult` is returned, indicating that the deletion was successful.

Here are the other classes in `Logic` (omitted from the class diagram above) that are used for parsing a user command:

<img src="images/ParserClasses.png" width="600"/>

How the parsing works:
* When called upon to parse a user command, the `AddressBookParser` class creates an `XYZCommandParser` (`XYZ` is a placeholder for the specific command name e.g., `AddCommandParser`) which uses the other classes shown above to parse the user command and create a `XYZCommand` object (e.g., `AddCommand`) which the `AddressBookParser` returns back as a `Command` object.
* All `XYZCommandParser` classes (e.g., `AddCommandParser`, `DeleteCommandParser`, ...) inherit from the `Parser` interface so that they can be treated similarly where possible e.g, during testing.

### Model component
**API** : [`Model.java`](https://github.com/AY2425S2-CS2103T-T12-4/tp/blob/master/src/main/java/seedu/address/model/Model.java)

<img src="images/ModelClassDiagram.png" width="450" />


The `Model` component,

* stores the address book data i.e., all `Person` objects (which are contained in a `UniquePersonList` object).
* stores the currently 'selected' `Person` objects (e.g., results of a search query) as a separate _filtered_ list which is exposed to outsiders as an unmodifiable `ObservableList<Person>` that can be 'observed' e.g. the UI can be bound to this list so that the UI automatically updates when the data in the list change.
* stores a `UserPref` object that represents the user’s preferences. This is exposed to the outside as a `ReadOnlyUserPref` objects.
* does not depend on any of the other three components (as the `Model` represents data entities of the domain, they should make sense on their own without depending on other components)

<div markdown="span" class="alert alert-info">:information_source: **Note:** An alternative (arguably, a more OOP) model is given below. It has a `Tag` list in the `AddressBook`, which `Person` references. This allows `AddressBook` to only require one `Tag` object per unique tag, instead of each `Person` needing their own `Tag` objects.<br>

<img src="images/BetterModelClassDiagram.png" width="450" />

</div>


### Storage component

**API** : [`Storage.java`](https://github.com/AY2425S2-CS2103T-T12-4/tp/blob/master/src/main/java/seedu/address/storage/Storage.java)

<img src="images/StorageClassDiagram.png" width="550" />

The `Storage` component,
* can save both address book data and user preference data in JSON format, and read them back into corresponding objects.
* inherits from both `AddressBookStorage` and `UserPrefStorage`, which means it can be treated as either one (if only the functionality of only one is needed).
* depends on some classes in the `Model` component (because the `Storage` component's job is to save/retrieve objects that belong to the `Model`)

### Common classes

Classes used by multiple components are in the `seedu.address.commons` package.

--------------------------------------------------------------------------------------------------------------------

## **Implementation**

This section describes some noteworthy details on how certain features are implemented.

### \[Proposed\] Undo/redo feature

#### Proposed Implementation

The proposed undo/redo mechanism is facilitated by `VersionedAddressBook`. It extends `AddressBook` with an undo/redo history, stored internally as an `addressBookStateList` and `currentStatePointer`. Additionally, it implements the following operations:

* `VersionedAddressBook#commit()` — Saves the current address book state in its history.
* `VersionedAddressBook#undo()` — Restores the previous address book state from its history.
* `VersionedAddressBook#redo()` — Restores a previously undone address book state from its history.

These operations are exposed in the `Model` interface as `Model#commitAddressBook()`, `Model#undoAddressBook()` and `Model#redoAddressBook()` respectively.

Given below is an example usage scenario and how the undo/redo mechanism behaves at each step.

Step 1. The user launches the application for the first time. The `VersionedAddressBook` will be initialized with the initial address book state, and the `currentStatePointer` pointing to that single address book state.

![UndoRedoState0](images/UndoRedoState0.png)

Step 2. The user executes `delete 5` command to delete the 5th person in the address book. The `delete` command calls `Model#commitAddressBook()`, causing the modified state of the address book after the `delete 5` command executes to be saved in the `addressBookStateList`, and the `currentStatePointer` is shifted to the newly inserted address book state.

![UndoRedoState1](images/UndoRedoState1.png)

Step 3. The user executes `add -n David …​` to add a new person. The `add` command also calls `Model#commitAddressBook()`, causing another modified address book state to be saved into the `addressBookStateList`.

![UndoRedoState2](images/UndoRedoState2.png)

<div markdown="span" class="alert alert-info">:information_source: **Note:** If a command fails its execution, it will not call `Model#commitAddressBook()`, so the address book state will not be saved into the `addressBookStateList`.

</div>

Step 4. The user now decides that adding the person was a mistake, and decides to undo that action by executing the `undo` command. The `undo` command will call `Model#undoAddressBook()`, which will shift the `currentStatePointer` once to the left, pointing it to the previous address book state, and restores the address book to that state.

![UndoRedoState3](images/UndoRedoState3.png)

<div markdown="span" class="alert alert-info">:information_source: **Note:** If the `currentStatePointer` is at index 0, pointing to the initial AddressBook state, then there are no previous AddressBook states to restore. The `undo` command uses `Model#canUndoAddressBook()` to check if this is the case. If so, it will return an error to the user rather
than attempting to perform the undo.

</div>

The following sequence diagram shows how an undo operation goes through the `Logic` component:

![UndoSequenceDiagram](images/UndoSequenceDiagram-Logic.png)

<div markdown="span" class="alert alert-info">:information_source: **Note:** The lifeline for `UndoCommand` should end at the destroy marker (X) but due to a limitation of PlantUML, the lifeline reaches the end of diagram.

</div>

Similarly, how an undo operation goes through the `Model` component is shown below:

![UndoSequenceDiagram](images/UndoSequenceDiagram-Model.png)

The `redo` command does the opposite — it calls `Model#redoAddressBook()`, which shifts the `currentStatePointer` once to the right, pointing to the previously undone state, and restores the address book to that state.

<div markdown="span" class="alert alert-info">:information_source: **Note:** If the `currentStatePointer` is at index `addressBookStateList.size() - 1`, pointing to the latest address book state, then there are no undone AddressBook states to restore. The `redo` command uses `Model#canRedoAddressBook()` to check if this is the case. If so, it will return an error to the user rather than attempting to perform the redo.

</div>

Step 5. The user then decides to execute the command `list`. Commands that do not modify the address book, such as `list`, will usually not call `Model#commitAddressBook()`, `Model#undoAddressBook()` or `Model#redoAddressBook()`. Thus, the `addressBookStateList` remains unchanged.

![UndoRedoState4](images/UndoRedoState4.png)

Step 6. The user executes `clear`, which calls `Model#commitAddressBook()`. Since the `currentStatePointer` is not pointing at the end of the `addressBookStateList`, all address book states after the `currentStatePointer` will be purged. Reason: It no longer makes sense to redo the `add -n David …​` command. This is the behavior that most modern desktop applications follow.

![UndoRedoState5](images/UndoRedoState5.png)

The following activity diagram summarizes what happens when a user executes a new command:

<img src="images/CommitActivityDiagram.png" width="250" />

#### Design considerations:

**Aspect: How undo & redo executes:**

* **Alternative 1 (current choice):** Saves the entire address book.
  * Pros: Easy to implement.
  * Cons: May have performance issues in terms of memory usage.

* **Alternative 2:** Individual command knows how to undo/redo by
  itself.
  * Pros: Will use less memory (e.g. for `delete`, just save the person being deleted).
  * Cons: We must ensure that the implementation of each individual command are correct.

_{more aspects and alternatives to be added}_

### Find upcoming appointments

The `upcoming` feature under the `find` command allows users to filter and display only those persons who have future appointments scheduled. This functionality is useful for quickly identifying clients with pending appointments.

#### Overview

* Command format: `find upcoming`
* Filters out persons whose appointment dates are **before or equal** to the current date and time.
* Supports both date-only and full datetime formats (e.g., `2025-04-01` or `2025-04-01 14:30`).

#### Key Classes & Logic

1. `FindCommandParser`
    * Detects if the user input is exactly `"upcoming"` (case-insensitive).
    * If matched, it returns a `FindCommand` initialized with an `UpcomingAppointmentPredicate`.
    * This diverges from the usual `-n` (name) or `-d` (appointment date) prefixes.
2. `UpcomingAppointmentPredicate`
    * Implements `Predicate<Person>`.
    * Extracts the `appointmentDate` from each `Person`.
    * Attempts to parse the string as either:
      * a full datetime using format `yyyy-MM-dd HH:mm`, or
      * a date-only format `yyyy-MM-dd` (defaults to `00:00` for comparison).
    * Compares the parsed result to `LocalDateTime.now()` and returns `true` only if the appointment is strictly after the current moment.
3. `AppointmentDate`
   * Normalizes and validates user-inputted date strings.
   * Accepts and validates both formats using regex and Java's `DateTimeFormatter`.
   * Helps maintain consistent formatting throughout the system.
4. `FindCommand`
   * When executed, it passes the predicate to the model's `updateFilteredPersonList()`, causing the filtered list to update with only persons matching the predicate.

The following class diagram shows the relationship between key classes involved:

![UpcomingAppointmentClassDiagram](images/UpcomingAppointmentClassDiagram.png)

#### Design Considerations

* **Fault Tolerance:** Any parsing errors during predicate evaluation result in a safe `false` return, preventing the app from crashing.
* **Extensibility:** This approach cleanly separates predicates, allowing future filters (e.g., "past appointments", "appointments this week") to be added by introducing new `Predicate<Person>` classes.
* **Single Responsibility:** Each class follows the SRP principle—e.g., `FindCommandParser` handles parsing, `UpcomingAppointmentPredicate` handles logic, and `AppointmentDate` handles formatting.

--------------------------------------------------------------------------------------------------------------------

## **Documentation, logging, testing, configuration, dev-ops**

* [Documentation guide](Documentation.md)
* [Testing guide](Testing.md)
* [Logging guide](Logging.md)
* [Configuration guide](Configuration.md)
* [DevOps guide](DevOps.md)

--------------------------------------------------------------------------------------------------------------------

## **Appendix: Requirements**

### Product scope

**Target user profile**:

* Nurses who do home visits for elderly patients
* Needs to manage a significant number of contacts
* Is reasonably comfortable using CLI-based apps

**Value proposition**: The app will help nurses caring for elderly patients manage contact details of their home-visit patients within one platform.

<blockquote style="color: #333333;">
Note: At present, SilverCare is designed to manage patient contacts only. It does not support storing information for family members, guardians, or emergency contacts. This decision was made intentionally to keep the app focused and avoid ambiguity during patient record management.
</blockquote>

### User stories

Priorities: High (must have) - `* * *`, Medium (nice to have) - `* *`, Low (unlikely to have) - `*`

| Priority | As a …​                                     | I want to …​                                                    | So that I can…​                                                    |
|----------|---------------------------------------------|-----------------------------------------------------------------|--------------------------------------------------------------------|
| `* * *`  | user                                        | see usage instructions                                          | refer to instructions when I forget how to use the app             |
| `* * *`  | new user                                    | have a quick-start guide                                        | learn how to use the app with minimal confusion from the beginning |
| `* * *`  | user                                        | view a help window while using the app                          | access reminders of the commands                                   |
| `* * *`  | home-visit nurse                            | add a new patient’s contact details                             | keep track of their information and reach out when needed          |
| `* * *`  | home-vist nurse                             | add a new patient’s full information, including medical details | keep track of the patients I visit                                 |
| `* * *`  | home-visit nurse managing multiple patients | update an existing patient's information                        | always have accurate and up-to-date records                        |
| `* * *`  | home-visit nurse                            | add appointment dates for patients                              | know when each patient is scheduled for a visit                    |
| `* * *`  | home-visit nurse                            | add custom notes or details about a patient                     | remember important context like accessibility needs                |
| `* * *`  | home-visit nurse                            | store multiple conditions for a patient                         | keep track of their medical history                                |
| `* * *`  | home-visit nurse                            | add a list of prescribed medications                            | log the patient's current treatment                                |
| `* * *`  | user                                        | delete a patient                                                | clean up records I no longer need                                  |
| `* * *`  | user                                        | see a confirmation message before deleting a patient record     | avoid accidentally removing important data                         |
| `* * *`  | home-visit nurse during visits              | search for a patient by name                                    | find important information quickly during a visit                  |
| `* * *`  | user                                        | search for a patients using partial names                       | find a patient even if I don’t remember their full name            |
| `* * *`  | home-visit nurse                            | search for patients by appointment date                         | check who I need to visit on a specific day                        |
| `* * *`  | home-visit nurse                            | view all upcoming appointments                                  | plan and prepare for upcoming visits                               |
| `* * *`  | user                                        | have search results highlight matching text                     | quickly identify relevant records                                  |
| `* * *`  | user                                        | list all patients                                               | get a full overview of who is currently in the system              |
| `* * *`  | user                                        | clear all my patient records                                    | reset and start with a clean slate                                 |
| `* * *`  | user                                        | see a confirmation message before clearing all patient records  | avoid accidentally removing important data                         |
| `* *`    | home-visit nurse with many patients         | sort my patient list by name                                    | locate a person easily                                             |
| `* *`    | user                                        | search for patients by phone number                             | retrieve contact details even if I only have a number available    |
| `* *`    | user                                        | see a warning when I exceed the patient limit                   | know that the app has reached capacity                             |
| `* *`    | user                                        | get error messages that include example usage                   | quickly learn how to correct errors I made                         |
| `* *`    | security-conscious user                     | lock or hide sensitive patient contact details                  | prevent unauthorized individuals from accessing them               |
| `* *`    | user                                        | toggle between light and dark mode                              | choose a display that's comfortable for my eyes                    |
| `*`      | busy nurse                                  | categorize patients based on their conditions or priority level | focus on urgent cases first                                        |
| `*`      | home-visit nurse with many patients         | filter my patient list by location                              | optimize my travel schedule for house visits                       |


### Use cases

(For all use cases below, the **System** is the `SilverCare` and the **Actor** is the `nurse`, unless specified otherwise)

**Use Case 1: Add a new patient**

**Goal: Add a new patient’s contact and medical information.**

**MSS:**
1. Nurse provides the required patient information: name, phone number, address, and gender.
2. Nurse may also provide optional information: appointment date, medical condition, medicine issued and additional notes.
3. System validates the data provided.
4. System checks whether a patient with same identifying details already exists.
5. System creates a new patient record and assigns it a unique ID.
6. System confirms addition and displays the new patient in the patient list.
7. Use case ends.

**Extensions:**
   * 2a. System detects invalid or incomplete entered data.
     * 2a1. System requests for the correct data.
     * 2a2. Nurse enters new data
     * Steps 2a1-2a2 are repeated until the data entered are correct.
     * Use case resumes from step 3.
   * 3a. System detects duplicate patient
     * 3a1. System informs Nurse that the patient already exists.
     * 3a2. System cancels the add request.
     * Use case ends.

**Use case 2: Delete an existing patient**

**Goal: Delete an existing patient’s contact and medical information.**

**MSS**

1. Nurse requests to delete an existing patient contact.
2. Nurse provides the patient's index in list.
3. System validates the provided patient index.
4. If valid, System prompts Nurse to confirm the deletion.
5. Nurse confirms the deletion.
6. System deletes the corresponding patient record.
7. System informs Nurse that the deletion is successful.
8. Use case ends.

**Extensions**

* 3a. System detects invalid patient index.
    * 3a1. System requests for the correct data.
    * 3a2. Nurse enters new data
    * Steps 3a1-3a2 are repeated until the data entered are correct.
    * Use case resumes from step 4.

* 3b. System does not find a patient with the provided index.
    * 3b1. System informs the Nurse that no matching patient exists.
    * 3b2. System requests a different patient index.
    * 3b3. Nurse provides a new patient index.
    * Steps 3b1–3b3 are repeated until a matching patient is found.
    * Use case resumes from step 4.

* 4a. Nurse cancels deletion.
    * Nurse declines the confirmation prompt.
    * System informs Nurse that the deletion has been cancelled.
    * Use case ends.

* 4b. Nurse types unrecognised, non-command input for confirmation of delete.
  * 4b1. System re-prompts Nurse to confirm deletion (expects yes or no).
  * 4b2. Steps 4 continues until a valid response is provided.
  * Use case resumes from 5 (if yes) or 4a (if no).

* 4c. Nurse requests for another command.
  * 4c1. System assumes the deletion has been abandoned.
  * 4c2. System cancels the deletion request without deleting any patient.
  * Use case ends.

**Use case 3: Find a patient by name**

**Goal: Search for a patient’s record by their name.**

**MSS**

1. Nurse requests to search for a patient by name.
2. Nurse provides a name to search for.
3. System validates the provided name (e.g., non-empty, valid characters).
4. System searches for patient records containing the name (case-insensitive).
5. System displays a list of matching patient records.
6. Use case ends.

**Extensions**

* 3a. Nurse provides empty name query.
    * 3a1. System informs the Nurse that the name cannot be empty.
    * 3a2. System requests a valid name.
    * 3a3. Nurse provides a new search name.
    * Steps 3a1–3a3 are repeated until a valid name is entered.
    * Use case resumes from step 4.

* 4a. No matching patient records found.
    * 4a1. System informs the Nurse that no matching records were found.
    * 4a2. System requests a new search input.
    * 4a3. Nurse provides a new search name.
    * Steps 4a1–4a3 are repeated until a match is found or the search is cancelled.
    * Use case resumes from step 4 or ends if cancelled.

**Use case 4: Find a patient by appointment date**

**Goal: Search for a patient’s record by appointment date.**

**MSS**

1. Nurse requests to search for a patient by appointment date.
2. Nurse provides a date to search for.
3. System validates the provided date format.
4. System searches for patient records with matching appointment dates.
5. System displays a list of matching patient records.
6. Use case ends.

**Extensions**

* 3a. Nurse provides wrongly formatted date query.
    * 3a1. System informs the Nurse that the date format is wrong.
    * 3a2. System requests a correctly formatted date input.
    * 3a3. Nurse provides a new date.
    * Steps 3a1–3a3 are repeated until a valid date is entered.
    * Use case resumes from step 4.

* 3b. Nurse provides invalid date query.
    * 3b1. System informs the Nurse that the date entered does not exist.
    * 3b2. System requests a valid date input.
    * 3b3. Nurse provides a new date.
    * Steps 3b1–3b3 are repeated until a valid date is entered.
    * Use case resumes from step 4.

* 4a. No matching patient records found.
    * 4a1. System informs the Nurse that no matching records were found.
    * Use case ends.

**Use case 5: Find upcoming appointments**

**Goal: Retrieve all patient records with appointment dates that are scheduled after the current system time.**

**MSS**

1. Nurse requests to view upcoming appointments.
2. System retrieves the current date and time.
3. System searches for all patients whose appointment dates are scheduled after the current system time.
4. System displays a list of matching patient records.
5. Use case ends.

**Extensions**

* 3a. No upcoming appointments found.
    * 3a1. System informs Nurse that there are no upcoming appointments.
    * Use case ends.

**Use case 6: Edit a patient record**

**Goal: Modify an existing patient's contact or medical information.**

**MSS**

1. Nurse requests to edit a patient record
2. Nurse provides the patient index and specifies one or more fields to update (e.g., name, phone, address, gender, etc.).
3. System validates the patient index and each field to be updated.
4. System updates the patient record with the new information.
5. System confirms the successful update.
6. Use case ends.

**Extensions**

* 3a. Invalid patient index provided
    * 3a1. System informs Nurse that the command is invalid or patient index is out of range.
    * 3a2. System requests a valid patient index.
    * 3a3. Nurse provides a new index.
    * Steps 3a1–3a3 repeat until a valid patient index is provided.
    * Use case resumes from step 3.

* 3b. Invalid input for one or more fields
    * 3b1. System informs the Nurse that the command is invalid.
    * 3b2. System requests corrected values for the invalid fields.
    * 3b3. Nurse provides corrected input.
    * Steps 3b1–3b3 repeat until all inputs are valid.
    * Use case resumes from step 4.

* 3c. No fields updated
    * 3c1. System informs Nurse that at least one field has to be edited.
    * 3a2. System requests at least one field to be updated.
    * 3a3. Nurse provides a new valid input.
    * Steps 3a1–3a3 repeat until a valid input is provided.
    * Use case resumes from step 3.

**Use case 7: Clear all patient records**

**Goal: Remove all existing patient records from the system.**

**MSS**

1. Nurse requests to clear all patient records.
2. System prompts Nurse for confirmation before proceeding.
3. Nurse confirms the action.
4. System deletes all patient records from the system.
5. System informs Nurse that the address book has been cleared.
6. Use case ends.

**Extensions**

* 3a. Nurse cancels the clear request
    * 3a1. Nurse declines the confirmation prompt.
    * 3a2. System informs Nurse that the clear action has been cancelled.
    * Use case ends.

* 4a. No records to clear
    * 4a1. System detects that there are no patient records to delete.
    * 4a2. System informs the Nurse that the address book is already empty.
    * Use case ends.

**Use case 8: List all patients**

**Goal: View all active patient records.**

**MSS**

1. Nurse requests to view all patient records.
2. System retrieves all stored patient records.
3. System displays the list of patients.
4. Use case ends.

**Extensions**

* 2a. No patients found in the system.
    * 2a1. System informs the Nurse that there are no patient records available.
    * Use case ends.

**Use case 9: Toggle application theme**

**Goal: Switch the application’s appearance between light and dark mode.**

**MSS**

1. Nurse toggles the application theme.
2. System determines the current theme (light or dark).
3. System switches to the opposite theme.
4. System applies the new theme across the user interface.
5. Use case ends.

**Use case 10: Display help information**

**Goal: Provide guidance to Nurse on how to use the system’s commands and features.**

**MSS**

1. Nurse requests to view help information.
2. System opens a help window.
3. System displays example commands and usage instructions.
4. System provides a hyperlink to view the full user guide.
5. Use case ends.

**Use case 11: Exit the application**

**Goal: Safely terminate the SilverCare application.**

**MSS**

1. Nurse requests to exit the application.
2. System saves any unsaved data.
3. System terminates the application.
4. Use case ends.

### Non-Functional Requirements

1. Should work on any _mainstream OS_ as long as it has Java `17` or above installed.
2. The system should efficiently handle at least 30 patient records, with all operations (e.g., search, add, delete) completing within 1 second on typical hardware.
3. A user with above average typing speed for regular English text (i.e. not code, not system admin commands) should be able to accomplish most of the tasks faster using commands than using the mouse.
4. The system should be able to search patient records within 2 seconds.
5. The interface should be accessible and navigable by nurses with basic training within 1 hour.
   The app should allow viewing patient details even without an internet connection.
6. The system’s user interface (UI) and documentation must be clear, comprehensive, and understandable for nurses and healthcare professionals without advanced IT skills.
7. The app is not required to connect to the hospital’s database.
8. The application must function fully offline, with all patient details accessible without an internet connection.
9. The app is not required to handle phone calls to the contact numbers of entries.
10. The application should gracefully handle invalid inputs and unexpected errors, providing clear error messages without crashing.

### Glossary

* **Mainstream OS**: Windows, Linux, Unix, MacOS
* **User**: A home-visit nurse who interacts with the SilverCare system to manage patient information.
* **New User**: A first-time user of the system who may require guidance on how to use the application.
* **Home-Visit Nurse**: A nurse who provides medical care to elderly patients in their homes, rather than at a hospital or clinic.
* **Patient Management**: The ability to store, update, and retrieve patient details such as name, contact information, and medical history.
* **Contact Details**: A patient’s phone number, address, and any other means of communication stored within the system.
* **Private contact detail**: A contact detail that is not meant to be shared with others
* **Patient Record**: A stored entry containing a patient's personal details, contact information, and relevant medical notes.
* **Unique Identifier (Patient Index)**: A system-generated number assigned to each patient record to ensure easy identification and management.


--------------------------------------------------------------------------------------------------------------------

## **Appendix: Instructions for manual testing**

Given below are instructions to test the app manually.

<div markdown="span" class="alert alert-info">:information_source: **Note:** These instructions only provide a starting point for testers to work on;
testers are expected to do more *exploratory* testing.

</div>

### Launch and shutdown

1. **Initial Launch**
    1. Download the JAR file and copy it into an empty folder.
    2. Open your **Command Prompt** (Windows) or **Terminal** (Mac/Linux) and navigate to the folder with the JAR file.
       ```sh
       cd path/to/file
       ```
    3. Run the following command:
       ```sh
       java -jar silvercare.jar
       ```
    4. **Expected:** The GUI appears with a set of sample patient records.



2. **Shutdown**
    1. Close the application using the exit command:
       ```sh
       exit
       ```
    2. **Expected:** The application closes successfully.

### Managing Patients


### Adding a Patient
- **Prerequisites:**
  - Adding a patient must be done with all compulsory fields (name, phone, address, gender).
  - Command format for each field should follow as stated in [User Guide](UserGuide.md#field-options)

    
- **Test case 1 (With compulosry fields):**
  ```sh
  add -n John Doe -p 91234567 -a 123 Clementi Ave 34 -g Male
  ```
  **Expected:** John Doe appears in the patient list with correct details.



- **Test case 2 (With optional fields):**
  ```sh
  add -n Johnny -p 92345678 -a 123 Clementi Ave 34 -g Male -d 2025-04-05 -c High BP -det lives alone -med Panadol
  ```
  **Expected:** Johnny appears in the patient list with correct details.



### Editing a Patient
- **Prerequisites:**
    - Index given must be positive (1,2,3...) and not out of range of list of patients.
    - Command format for each field should follow as stated in [User Guide](UserGuide.md#field-options)


- **Test case 1 (Edits name of patient at index 1):**
  ```sh
  list
  edit 1 -n Bobby
  ```
  **Expected:** The name of the first patient index updates correctly to Bobby.


- **Test case 2 (Edits name and phone number of patient at index 1):**
  ```sh
  list
  edit 1 -n Bobby -p 91283131
  ```
  **Expected:** The name and phone number of the first patient index updates correctly to Bobby and 91283131 respectively.


- **Test case 3 (Edits condition of patient at index 2 to the 3 new conditions stated):**
  ```sh
  list
  edit 2 -c Dementia -c Asthma -c Diabetic
  ```
  **Expected:** The condition of the second patient index updates correctly to the three stated as above.


- **Test case 4 (Invalid Command):**
  ```sh
  edit hi
  ```
  **Expected:** Error message pops up due to incorrect formatting.

### Deleting a Patient
- **Prerequisites:** 
  - Index given must be positive (1,2,3...) and not out of range of list of patients.


- **Test case 1 (Successfully deletes patient at index 1):**
  ```sh
  list
  delete 1
  y
  ```
  **Expected:** A confirmation prompt pops up and successfully deletes patient 1 after inputting [y].


- **Test case 2 (Abort deletion of patient at index 1):**
  ```sh
  list
  delete 1
  n
  ```
  **Expected:** A confirmation prompt pops up and aborts deleting patient 1 after inputting [n].


- **Test case 3 (Invalid command):**
  ```sh
  list
  delete HI
  ```
  **Expected:** Error message pops up due to incorrect formatting.


---

## Searching and Filtering
- **Prerequisites:**
    - Index given must be positive (1,2,3...) and not out of range of list of patients.
    - Command format should follow as stated in [User Guide](UserGuide.md#locating-persons-by-name-find)


- **Test case 1 (Find by name):**
  ```sh
  list 
  find -n John
  ```
  **Expected:** Only matching patients containing name "John" are shown.


- **Test case 2 (Find by date):**
  ```sh
  list
  find -d 2025-04-12
  ```
  **Expected:** Only matching patients with appointment date of "2025-04-12" are shown.


- **Test case 3 (Find upcoming appointments):**
  ```sh
  list
  find upcoming
  ```
  **Expected:** Only matching patients with appointment dates after current system timing are shown.

---

## Data Handling

### Saving Data
- **Expected:** Changes persist after restarting the application.

### Handling Missing/Corrupted Files
- **To simulate:** Delete or corrupt the data file before launching the app.
- **Expected:** The application should recover gracefully and provide an error message or reset data appropriately.

---

## Edge Cases

### Invalid Inputs:
- Enter an invalid phone number (`-p abcd1234`).
- Delete a non-existent patient.  
  **Expected:** Proper error messages.



Testers should verify that expected results match actual outcomes and report any inconsistencies as bugs. 

Ensure that all covered features work as intended while considering possible edge cases. 

For full command details, refer to the [User Guide](UserGuide.md).


--------------------------------------------------------------------------------------------------------------------

## **Appendix: Effort**
The development of **SilverCare** involved adapting and extending the AddressBook-Level3 codebase to better suit the needs of home-visit nurses.
The team focused on improving usability, expanding patient-related functionality, and ensuring the application remained robust and maintainable.

**Key development efforts included:**

* Enhancing core commands (`add`, `edit`, `find`, etc.) to support patient-specific data such as appointment dates, conditions, and medication

* Refactoring the model and storage structure to accommodate new fields and tag types

* Updating and writing unit tests to ensure system correctness after changes

* Streamlining the user interface and user guide to better support a non-technical audience

The team maintained regular weekly check-ins to coordinate tasks, discuss challenges, and review progress.
All members contributed equally across design, implementation, and testing phases, ensuring a balanced and collaborative workflow.

--------------------------------------------------------------------------------------------------------------------

## **Appendix: Planned Enhancements**

Team size: 5
### 1. Disallow adding past appointment dates
**Current Limitation:** The system currently allows users to add appointment dates that have already passed.

**Planned Enhancement:** Restrict users from entering past dates for new or edited appointments.

**Sample Output:**
`Appointment date must be today or later.`

### 2. Make the medicine field more structured and repeatable
**Current Limitation:**
The medicine field is currently a single long string, which accepts comma-separated medicine names but offers no structure or repeatability.
Users cannot add medicine entries separately or include details like dosage and frequency.

**Planned Enhancement:**
Allow the `-med` field to be repeatable and support structured input for each entry (e.g. name, dosage, frequency).
This makes it easier to store, update, and display individual medication records.

**Sample Input (future):**
`-med Paracetamol (500mg, twice a day) -med Omeprazole (20mg, once daily)`

### 3. Improve input validation for name-based search
**Current Limitation:**
The `find -n` command accepts any non-empty input and defaults to “no patients found.”

**Planned Enhancement:**
Detect clearly invalid inputs (e.g. empty, all-symbol, extremely short) and display a more meaningful error message.

**Sample Output:** `Search term is invalid. Please enter a proper name.`

### 4. Allow appointment date to be cleared using the edit command
**Current Limitation:**
Unlike `-c` and `-det`, providing an empty `-d` does not clear an existing appointment date.

**Planned Enhancement:**
Support clearing the appointment date by accepting `-d` with no argument during edit.

**Sample Input:** `edit 1 -d  `

### 5. Add find-and-replace functionality for editing tags
**Current Limitation:**
Editing a single condition or detail tag requires re-entering all tags, as the current logic replaces the entire existing list.

**Planned Enhancement:**
Introduce a `find-and-replace` syntax to update a specific tag in-place.

**Sample Input:**
`edit 2 -c Migraine->Chronic Migraine -det Lives Alone->Requires Assistance`

### 6. Warn users about overlapping appointments
**Current Limitation:**
There is no indication if multiple appointments are scheduled for the same date and time.

**Planned Enhancement:**
Detect and warn the user when a newly entered appointment overlaps with an existing one for the same patient.

**Sample Output:**
`Warning: This appointment overlaps with another on 2025-07-20 at 14:00.`

### 7. Fix highlight issue in multi-word name searches
**Current Limitation:**
When using `find -n` with multi-word input (e.g. John Lim), matches are returned but highlighted text only works for single words.

**Planned Enhancement:**
Improve result rendering to highlight all matched words in multi-keyword searches.

### 8. Enable multi-name search to match across multiple patients
**Current Limitation:**
`find -n Bob Amy` only returns matches where both keywords are found in the same patient name. It does not return separate matches for “Bob” and “Amy.”

**Planned Enhancement:**
Allow find -n to return results for any of the keywords (logical OR), not just matches containing all of them.
