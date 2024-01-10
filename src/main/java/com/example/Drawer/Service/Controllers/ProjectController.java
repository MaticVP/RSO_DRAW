package com.example.Drawer.Service.Controllers;

import com.dropbox.core.DbxException;
import com.example.Drawer.Service.Entity.Project;
import com.example.Drawer.Service.Services.DropboxService;
import com.example.Drawer.Service.Services.ProjectService;
import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import io.github.resilience4j.retry.annotation.Retry;
import io.github.resilience4j.timelimiter.annotation.TimeLimiter;
import io.swagger.v3.oas.annotations.OpenAPIDefinition;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.info.Info;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import org.apache.tomcat.util.codec.binary.Base64;
import org.hibernate.cache.spi.support.AbstractReadWriteAccess;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Objects;

@Controller
@RequestMapping(path="api/draw")
@CrossOrigin(origins = "http://localhost:3000")
@OpenAPIDefinition(
        info = @Info(
                title = "Drawer API",
                version = "1.0",
                description = "API responsible for handling request related to image drawing and projects (adding project, saving image etc.)"
        )
)
@Tag(name = "Drawer API", description = "API responsible for handling request related to image drawing and projects (adding project, saving image etc.)")
public class ProjectController {

    Logger logger = LoggerFactory.getLogger(ProjectController.class);
    public static String UPLOAD_DIRECTORY = System.getProperty("user.dir") + "/uploads";

    @Autowired
    private com.example.Drawer.Service.Repository.ProjectRepository projectRepository;

    @Autowired
    private ProjectService projectService;

    @Autowired
    DropboxService dropboxService = new DropboxService();

    @PostMapping(path="/add")
    public @ResponseBody
    @Operation(summary = "Add project")
    @ApiResponse(responseCode = "200", description = "Successful added a new project", content = @Content(schema = @Schema(implementation = AbstractReadWriteAccess.Item.class)))
    @CircuitBreaker(name = "CircuitBreakerService")
    String uploadProject (@RequestParam String project_name, @RequestParam String username,@RequestParam int width,@RequestParam int height) {
        logger.info("Entering (uploadProject)");
        long id = Long.parseLong(projectService.getIDFromUserMicroservice(username));
        Project image = new Project(project_name,id, width, height);
        projectRepository.save(image);
        logger.info("Exiting (uploadProject): User added to DB");
        return "Saved";
    }

    @PostMapping(path="/add-by-id")
    public @ResponseBody
    @Operation(summary = "Add project by providing user id")
    @ApiResponse(responseCode = "200", description = "Successful added a new project by id", content = @Content(schema = @Schema(implementation = AbstractReadWriteAccess.Item.class)))
    @CircuitBreaker(name = "CircuitBreakerService")
    String uploadProject (@RequestParam String project_name, @RequestParam long id,@RequestParam int width,@RequestParam int height) {
        logger.info("Entering (uploadProject)");
        Project image = new Project(project_name, id, width, height);
        projectRepository.save(image);
        logger.info("Exiting (uploadProject): User added to DB");
        return "Saved";
    }

    @PostMapping(path="/save-project")
    @Operation(summary = "Upload the image drawn in React")
    @ApiResponse(responseCode = "200", description = "Successful uploaded image to Dropbox API", content = @Content(schema = @Schema(implementation = AbstractReadWriteAccess.Item.class)))
    @ApiResponse(responseCode = "503", description = "Something related to dropbox failed Dropbox API", content = @Content(schema = @Schema(implementation = AbstractReadWriteAccess.Item.class)))
    @ResponseBody
    public String uploadImage (@RequestParam("project_name") String project_name, @RequestParam("username") String username, @RequestParam("image") String file, HttpServletRequest request) throws IOException {
        logger.info("Entering (uploadImage): drop box added image");
        project_name+=".png";
        String image_data = file.substring(file.indexOf(",")+1);
        byte[] imageByte= Base64.decodeBase64(image_data);
        InputStream is = new ByteArrayInputStream(imageByte);
        dropboxService.saveImage(is, project_name,username);
        logger.info("Exiting (uploadImage): drop box added image");
        return "photo added";
    }

    @GetMapping(path="/get-project")
    @Operation(summary = "Returns image that was stored in Dropbox and is related to project_name")
    @ApiResponse(responseCode = "200", description = "Successful returned image to Dropbox API", content = @Content(schema = @Schema(implementation = AbstractReadWriteAccess.Item.class)))
    @ApiResponse(responseCode = "503", description = "Something related to dropbox failed Dropbox API", content = @Content(schema = @Schema(implementation = AbstractReadWriteAccess.Item.class)))
    public @ResponseBody
    @CrossOrigin(origins = "http://localhost:3000")
    ResponseEntity<byte[]> getProject(@RequestParam String project_name, @RequestParam String username) throws IOException {
        logger.info("Entering (getProject)");
        byte[] image = dropboxService.getImage(username,project_name);
        logger.info("drop box got image");
        logger.info("Exiting (getProject): Got project");
        return ResponseEntity.ok().contentType(MediaType.IMAGE_JPEG).body(image);
    }

    @GetMapping(path="/get-project-sick")
    @Operation(summary = "Example of a sick method. Should not be called")
    @ApiResponse(responseCode = "503", description = "Something related to dropbox failed Dropbox API", content = @Content(schema = @Schema(implementation = AbstractReadWriteAccess.Item.class)))
    public @ResponseBody
    @CrossOrigin(origins = "http://localhost:3000")
    ResponseEntity<byte[]> getProjectSick(@RequestParam String project_name, @RequestParam String username) throws IOException, DbxException {
        logger.info("Entering (getProject)");
        byte[] image = dropboxService.getImageBad(username,project_name);
        logger.info("drop box got image");
        logger.info("Exiting (getProject): Got project");
        return ResponseEntity.ok().contentType(MediaType.IMAGE_JPEG).body(image);
    }

    @GetMapping(path="/get-projects-by-id")
    @Operation(summary = "Returns list of projects that have there User ID same as the one that was given in REST")
    @ApiResponse(responseCode = "200", description = "Successful returned user's projects", content = @Content(schema = @Schema(implementation = AbstractReadWriteAccess.Item.class)))
    public @ResponseBody
    @CrossOrigin(origins = "http://localhost:3000")
    String[] getProjects (@RequestParam long id) {
        logger.info("Entering (getProjects)");
        Project[] projects = projectRepository.findByUserId(id);
        String[]  project_names = new String[projects.length];
        for (int i=0;i<projects.length;i++){
            project_names[i] = projects[i].getImage_name();
        }
        logger.info("Exiting (getProjects): Got projects");
        return project_names;
    }

    @GetMapping(path="/get-projects")
    @Operation(summary = "Returns list of projects that have there User ID. Username is given so we can find the User ID")
    @ApiResponse(responseCode = "200", description = "Successful returned user's projects", content = @Content(schema = @Schema(implementation = AbstractReadWriteAccess.Item.class)))
    @ApiResponse(responseCode = "503", description = "Circuit breaker is in open state", content = @Content(schema = @Schema(implementation = AbstractReadWriteAccess.Item.class)))
    @ApiResponse(responseCode = "500", description = "Trouble calling GraphQL on User microservice", content = @Content(schema = @Schema(implementation = AbstractReadWriteAccess.Item.class)))
    public @ResponseBody
    @CrossOrigin(origins = "http://localhost:3000")
    String[] getProjects (@RequestParam String username) {
        logger.info("Entering (getProjects)");
        long id = Long.parseLong(projectService.getIDFromUserWithGrapQL(username));
        Project[] projects = projectRepository.findByUserId(id);
        String[]  project_names = new String[projects.length];
        for (int i=0;i<projects.length;i++){
            project_names[i] = projects[i].getImage_name();
        }
        logger.info("Exiting (getProjects): Got projects");
        return project_names;
    }

    @PostMapping(path="/save-projects")
    @Operation(summary = "Saves the project metadata")
    @ApiResponse(responseCode = "200", description = "Successful saved user's project metadata", content = @Content(schema = @Schema(implementation = AbstractReadWriteAccess.Item.class)))
    public @ResponseBody
    @CrossOrigin(origins = "http://localhost:3000")
    String saveProjectInfo (@RequestParam String project_name,@RequestParam String username,@RequestParam int width,@RequestParam int height) {
        logger.info("Entering (saveProjectInfo)");
        long id = Long.parseLong(projectService.getIDFromUserMicroservice(username));
        Project image = new Project(project_name,id, width, height);
        projectRepository.save(image);
        logger.info("Exiting (saveProjectInfo): added new project to DB");
        return "Saved";
    }

    @PostMapping(path="/save-projects-by-id")
    @Operation(summary = "Saves the project metadata but used user id as parameter")
    @ApiResponse(responseCode = "200", description = "Successful saved user's project metadata", content = @Content(schema = @Schema(implementation = AbstractReadWriteAccess.Item.class)))
    public @ResponseBody
    @CrossOrigin(origins = "http://localhost:3000")
    String saveProjectInfo (@RequestParam String project_name,@RequestParam long id, @RequestParam int width,@RequestParam int height) {
        logger.info("Entering (saveProjectInfo)");
        Project[] projects = projectRepository.findByUserId(id);
        for (Project project : projects) {
            if (Objects.equals(project.getImage_name(), project_name)) {
                logger.info("Exiting (saveProjectInfo): project is DB");
                return "Saved";
            }
        }

        Project image = new Project(project_name,id, width, height);
        projectRepository.save(image);
        logger.info("Exiting (saveProjectInfo): added new project to DB");
        return "Saved";
    }

    @Operation(summary = "Deletes all projects")
    @ApiResponse(responseCode = "200", description = "Successful removed all projects", content = @Content(schema = @Schema(implementation = AbstractReadWriteAccess.Item.class)))
    @DeleteMapping(path="/deleteAll")
    @CrossOrigin(origins = "http://localhost:3000")
    public String deleteAllProject() {
        logger.info("Entering (deleteAllProject)");
        logger.info("Deleting projects");
        projectRepository.deleteAll();
        logger.info("Exiting (deleteAllProject)");
        return "Deleted all projects";

    }

    @Operation(summary = "Deletes single project")
    @ApiResponse(responseCode = "200", description = "Successful removed project", content = @Content(schema = @Schema(implementation = AbstractReadWriteAccess.Item.class)))
    @DeleteMapping(path="/delete")
    @CrossOrigin(origins = "http://localhost:3000")
    @Transactional

    public ResponseEntity<?> deleteAllProject(@RequestParam String project_name, @RequestParam String username) {
        try {
            logger.info("Entering (deleteSingleProject)");
            long id = Long.parseLong(projectService.getIDFromUserWithGrapQL(username));
            Project[] projects = projectRepository.findByUserId(id);
            for (int i=0;i<projects.length;i++){
                if(projects[0].getImage_name().equals(project_name)) {
                    projectRepository.removeByProjectName(projects[0].getImage_name());
                }
            }
            logger.info("Exiting (deleteSingleProject)");
            return ResponseEntity.status(HttpStatus.OK).build();
        }
        catch (Exception e){
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }

    }

    @Operation(summary = "Returns all projects")
    @ApiResponse(responseCode = "200", description = "Successful returned projects", content = @Content(schema = @Schema(implementation = AbstractReadWriteAccess.Item.class)))
    @GetMapping(path="/all")
    @CrossOrigin(origins = "http://localhost:3000")
    public @ResponseBody Iterable<Project> getAllProject() {
        logger.info("Entering (getAllProject)");
        logger.info("Getting all projects");
        logger.info("Exiting (getAllProject)");
        return projectRepository.findAll();

    }
}


