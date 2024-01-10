package com.example.Drawer.Service.Controllers;

import com.dropbox.core.DbxException;
import com.example.Drawer.Service.Entity.Project;
import com.example.Drawer.Service.Services.DropboxService;
import com.example.Drawer.Service.Services.ProjectService;
import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import io.github.resilience4j.retry.annotation.Retry;
import io.github.resilience4j.timelimiter.annotation.TimeLimiter;
import jakarta.servlet.http.HttpServletRequest;
import org.apache.tomcat.util.codec.binary.Base64;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Objects;

@Controller
@RequestMapping(path="api/draw")
@CrossOrigin(origins = "http://localhost:3000")
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
    @CircuitBreaker(name = "CircuitBreakerService")
    String uploadProject (@RequestParam String project_name, @RequestParam long id,@RequestParam int width,@RequestParam int height) {
        logger.info("Entering (uploadProject)");
        Project image = new Project(project_name, id, width, height);
        projectRepository.save(image);
        logger.info("Exiting (uploadProject): User added to DB");
        return "Saved";
    }

    @PostMapping(path="/save-project")
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




    @DeleteMapping(path="/deleteAll")
    public String deleteAllProject() {
        logger.info("Deleting users");
        projectRepository.deleteAll();
        return "Deleted all users";

    }

    @DeleteMapping(path="/delete")
    public String deleteAllProject(String project_name) {
        projectRepository.removeByProjectName(project_name);
        return "Deleted deleted user "+project_name;

    }

    @GetMapping(path="/all")
    public @ResponseBody Iterable<Project> getAllProject() {
        logger.info("Getting all projects");
        return projectRepository.findAll();

    }
}


