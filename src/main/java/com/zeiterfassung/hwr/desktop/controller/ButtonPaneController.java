package com.zeiterfassung.hwr.desktop.controller;

import com.zeiterfassung.hwr.desktop.component.views.ButtonPane;
import com.zeiterfassung.hwr.desktop.entities.Project;
import com.zeiterfassung.hwr.desktop.entities.TimeAction;
import javafx.application.Platform;
import javafx.beans.value.ChangeListener;
import javafx.collections.FXCollections;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import org.jetbrains.annotations.NotNull;
import org.springframework.stereotype.Controller;
import org.springframework.web.reactive.function.client.ClientResponse;
import reactor.core.publisher.Mono;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.Function;

import static com.zeiterfassung.hwr.desktop.controller.ButtonPaneController.ViewStatus.*;

/**
 * The Button pane controller.
 */
@Controller
public class ButtonPaneController
{
    private ButtonPane view;
    private WebClientController webClientController;
    private int currentProjectId;
    private ViewStatus currentViewStatus;

    /**
     * The enum View status.
     */
    enum ViewStatus
    {
        /**
         * Workperiod view status.
         */
        WORKPERIOD,
        /**
         * Break view status.
         */
        BREAK,
        /**
         * Interruption view status.
         */
        INTERRUPTION}

    /**
     * Instantiates a new Button pane controller.
     *
     * @param buttonPane          the button pane
     * @param webClientController the web client controller
     */
    public ButtonPaneController(ButtonPane buttonPane, WebClientController webClientController)
    {
        this.view = buttonPane;
        this.webClientController = webClientController;
    }

    /**
     * init set controller.
     */
    public void setController()
    {
        Optional<TimeAction> lastBookedTime = webClientController.fetchTodaysLastBookedTime();
        List<Project> projects = webClientController.fetchProjects();
        Map<String, String> user = webClientController.fetchUserName();

        Optional<String> lastProjectName = getOptionalLastProjectName(lastBookedTime, projects);
        List<String> projectNames = getProjectNames(projects);
        Optional<String> alphanumericalFirstProjectName = getAlphanumericalFirstProjectName(projectNames);

        initializeViewStatus(lastBookedTime);
        initializeGreetingLabel(user);
        initializeBtnProject(lastProjectName, alphanumericalFirstProjectName, projectNames);

        setBtnEventHandler(projects);
    }

    /**
     * get the optional last Project name
     *
     * @param lastBookedTime    the last booked time
     * @param projects  the projects
     * @return Optional project name
     */
    @NotNull
    private Optional<String> getOptionalLastProjectName(Optional<TimeAction> lastBookedTime, List<Project> projects)
    {
        return projects.stream()
                .filter(project ->
                {
                    if (lastBookedTime.isPresent())
                    {
                        return project.getId() == lastBookedTime.get().getProjectId();
                    }
                    return false;
                })
                .findFirst()
                .map(Project::getName);
    }

    /**
     * get the project Names from a list of projects OR map list of projects to the project names
     *
     * @param projects  the list of project
     * @return list of project names
     */
    private List<String> getProjectNames(List<Project> projects)
    {
        return projects.stream()
                .map(Project::getName)
                .sorted()
                .toList();
    }

    /**
     * get the alphanumerical first Project name of a list of project names
     *
     * @param projectNames  list of project names
     * @return Optional project Name
     */
    @NotNull
    private Optional<String> getAlphanumericalFirstProjectName(List<String> projectNames)
    {
        return projectNames.stream()
                .min(String::compareTo);
    }

    /**
     * initialize the view status
     *
     * @param lastBookedTime    the last booked Time
     */
    private void initializeViewStatus(Optional<TimeAction> lastBookedTime)
    {
        if (lastBookedTime.isPresent())
        {
            boolean isStart = lastBookedTime.get().isStart();
            boolean isBreak = lastBookedTime.get().isBreak();
            currentProjectId = lastBookedTime.get().getProjectId();

            if (isStart && !isBreak)
            {
                setBtnStatus(true, false, false);
                currentViewStatus = WORKPERIOD;

            } else if (!isStart && !isBreak)
            {
                setBtnStatus(false, true, true);
                currentViewStatus = INTERRUPTION;
            } else if (isStart && isBreak)
            {
                setBtnStatus(false, true, true);
                currentViewStatus = BREAK;
            } else
            {
                throw new IllegalStateException("Last time can't be a finished Break Exception");
            }
        } else
        {
            currentViewStatus = WORKPERIOD;
            currentProjectId = -1;
        }
    }

    /**
     * initialize greeting label
     *
     * @param user user as map with first and last name
     */
    private void initializeGreetingLabel(Map<String, String> user)
    {
        String greeting = "Hi " + user.get("firstName") + " " + user.get("lastName");
        view.getGreetingLabel().setText(greeting);
    }

    /**
     * initialize btn project
     *
     * @param lastProjectName   the last project name
     * @param alphanumericalFirstProjectName    the alphanumerical First Project Name
     * @param projectNames  the project Names
     */
    private void initializeBtnProject(Optional<String> lastProjectName,
                                      Optional<String> alphanumericalFirstProjectName,
                                      List<String> projectNames)
    {
        view.getBtnProject().setItems(FXCollections.observableArrayList(projectNames));
        view.getBtnProject().setValue(lastProjectName.orElse(alphanumericalFirstProjectName.orElse("Projekt")));
    }

    /**
     * set Event handlers for all Buttons (Start, End, Break, Projects)
     *
     * @param projects  the list of projects
     */
    private void setBtnEventHandler(List<Project> projects)
    {
        view.getBtnStart().setOnAction(startEventHandler(projects));
        view.getBtnEnd().setOnAction(endEventHandler(projects));
        view.getBtnBreak().setOnAction(breakEventHandler(projects));
        view.getBtnProject().getSelectionModel()
                .selectedItemProperty()
                .addListener(projectChangeListener(projects));
    }

    /**
     * Startbtn Event-handler
     *
     * @param projects list of projects
     * @return Event Handler of Action Event
     */
    @NotNull
    private EventHandler<ActionEvent> startEventHandler(List<Project> projects)
    {
        return btnClick ->
        {
            view.getErrorLabel().setVisible(false);
            int projectID = getProjectID(projects);

            if (currentViewStatus == BREAK)
            {
                // postrequest Pausenende
                webClientController.postTime(false, true, projectID, noResponse, errorResponse);
            }

            // Automatisch Arbeitszeitblock bei Projektwechsel beenden
            if (currentProjectId > 0 && currentProjectId != projectID && currentViewStatus == WORKPERIOD)
            {
                //postrequest Arbeitszeitblockende automatisch bei Projektwechel des alten Projektes
                webClientController.postTime(false, false, currentProjectId, noResponse, errorResponse);
            }

            //postrequest Arbeitszeitblockstart
            webClientController.postTime(true, false, projectID, clientResponse ->
            {
                currentProjectId = projectID;

                Platform.runLater(() ->
                {
                    currentViewStatus = WORKPERIOD;
                    view.getBtnStart().setText("Arbeit fortsetzen");
                    setBtnStatus(true, false, false);
                    if (view.getProjectLabel().isVisible())
                    {
                        view.getProjectLabel().setVisible(false);
                    }
                });

                return Mono.empty();
            }, errorResponse);
        };
    }

    /**
     * Endbtn Event-handler
     *
     * @param projects list of projects
     * @return Event Handler of Action Event
     */
    @NotNull
    private EventHandler<ActionEvent> endEventHandler(List<Project> projects)
    {
        return click ->
        {
            view.getErrorLabel().setVisible(false);

            int projectID = getProjectID(projects);

            //postrequest Arbeitszeitblockende durch User manuell
            webClientController.postTime(false, false, projectID, clientResponse ->
            {
                Platform.runLater(() ->
                {
                    currentViewStatus = INTERRUPTION;
                    setBtnStatus(false, true, true);
                });
                return Mono.empty();
            }, errorResponse);
        };
    }

    /**
     * Breakbtn Event-handler
     *
     * @param projects list of projects
     * @return Event Handler of Action Event
     */
    private EventHandler<ActionEvent> breakEventHandler(List<Project> projects)
    {
        return btnClick ->
        {
            view.getErrorLabel().setVisible(false);

            int projectID = getProjectID(projects);

            //postrequest Arbeitszeitblockende automatisch beim Starten der Pause
            webClientController.postTime(false, false, projectID, noResponse, errorResponse);

            //postrequest Pausenbeginn
            webClientController.postTime(true, true, projectID, clientResponse ->
            {
                Platform.runLater(() ->
                {
                    currentViewStatus = BREAK;
                    setBtnStatus(false, true, true);
                });

                return Mono.empty();
            }, errorResponse);
        };
    }

    /**
     * Projectbtn Change-listener (Observer)
     *
     * @param projects list of projects
     * @return Event Handler of Action Event
     */
    @NotNull
    private ChangeListener<String> projectChangeListener(List<Project> projects)
    {
        return (change, oldValue, newValue) ->
        {
            //Auswahl des Projekts im Dropdownmenü
            int newProjectId = projects.stream()
                    .filter(project -> project.getName().equals(newValue))
                    .findFirst()
                    .map(Project::getId)
                    .orElseThrow();

            //managed BtnStart bei Projektwechsel + Info Label
            // (inkl. Enabled/Disabled, Label: "Arbeit fortsetzen"/"Projekt wechseln")
            if (newProjectId == currentProjectId)
            {
                view.getBtnStart().setText("Arbeit fortsetzen");
                view.getBtnStart().setDisable(false);
                view.getProjectLabel().setVisible(false);

                if (currentViewStatus == WORKPERIOD)
                {
                    view.getBtnStart().setDisable(true);
                }

            } else
            {
                if (currentProjectId != -1)
                {
                    view.getBtnStart().setDisable(false);
                    view.getBtnStart().setText("Projekt wechseln");

                    view.getProjectLabel().setVisible(true);
                }
            }
        };
    }

    /**
     * if the XYZ Button shall be disabled
     *
     * @param disabledBtnStart  if the Start Button is disabled
     * @param disabledBtnEnd    if the End Button is disabled
     * @param disabledBtnBreak  if the Break Button is disabled
     */
    private void setBtnStatus(Boolean disabledBtnStart, Boolean disabledBtnEnd, Boolean disabledBtnBreak)
    {
        view.getBtnStart().setDisable(disabledBtnStart);
        view.getBtnEnd().setDisable(disabledBtnEnd);
        view.getBtnBreak().setDisable(disabledBtnBreak);
    }

    /**
     * get ProjectID from selected Project (Projectbtn)
     *
     * @param projects list of projects
     * @return Project Id
     */
    private int getProjectID(List<Project> projects)
    {
        String selectedProjectName = view.getBtnProject().getValue();
        return projects.stream()
                .filter(project -> project.getName().equals(selectedProjectName))
                .findFirst()
                .map(Project::getId)
                .orElseThrow();
    }

    @NotNull
    private final Function<ClientResponse, Mono<? extends Throwable>> noResponse = clientResponse -> Mono.empty();

    @NotNull
    private final Function<ClientResponse, Mono<? extends Throwable>> errorResponse = clientResponse ->
    {
        Platform.runLater(() ->
        {
            String errorReason = clientResponse.statusCode().getReasonPhrase();
            view.getErrorLabel().setText("Error: " + errorReason);
            view.getErrorLabel().setVisible(true);
        });
        return Mono.empty();
    };

}
