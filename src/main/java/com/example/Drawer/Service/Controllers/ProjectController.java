package com.example.Drawer.Service.Controllers;

import com.example.Drawer.Service.Entity.Project;
import com.example.Drawer.Service.Services.DropboxService;
import com.example.Drawer.Service.Services.ProjectService;
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

@Controller
@RequestMapping(path="api/projects")
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
    @CrossOrigin(origins = "http://localhost:3000")
    String uploadProject (@RequestParam String project_name, @RequestParam String username,@RequestParam int width,@RequestParam int height) {
        long id = Long.parseLong(projectService.getIDFromUserMicroservice(username));
        Project image = new Project(project_name,id, width, height);
        projectRepository.save(image);
        return "Saved";
    }

    @PostMapping(path="/save-project")
    @CrossOrigin(origins = "http://localhost:3000")
    @ResponseBody
    public String uploadImage (@RequestParam("project_name") String project_name, @RequestParam("username") String username, @RequestParam("image") String file, HttpServletRequest request) throws IOException {
        project_name+=".png";
        String image_data = file.substring(file.indexOf(",")+1);
        byte[] imageByte= Base64.decodeBase64(image_data);
        InputStream is = new ByteArrayInputStream(imageByte);
        dropboxService.saveImage(is, project_name,username);
        logger.info("drop box added image");
        return "photo added";
    }

    @GetMapping(path="/get-project")
    public @ResponseBody
    @CrossOrigin(origins = "http://localhost:3000")
    ResponseEntity<byte[]> getProject(@RequestParam String project_name, @RequestParam String username, @RequestParam("image") MultipartFile file) throws IOException {
        byte[] image = dropboxService.getImage(username,project_name);
        logger.info("drop box got image");
        return ResponseEntity.ok().contentType(MediaType.IMAGE_JPEG).body(image);
    }

    @GetMapping(path="/get-projects")
    public @ResponseBody
    @CrossOrigin(origins = "http://localhost:3000")
    String[] getProjects (@RequestParam String username) {
        long id = Long.parseLong(projectService.getIDFromUserMicroservice(username));
        Project[] projects = projectRepository.findByUserId(id);
        String[]  project_names = new String[projects.length];
        for (int i=0;i<projects.length;i++){
            project_names[i] = projects[i].getImage_name();
        }
        return project_names;
    }

    @PostMapping(path="/save-projects")
    public @ResponseBody
    @CrossOrigin(origins = "http://localhost:3000")
    String saveProjectInfo (@RequestParam String project_name,@RequestParam String username,@RequestParam int width,@RequestParam int height) {
        long id = Long.parseLong(projectService.getIDFromUserMicroservice(username));
        Project image = new Project(project_name,id, width, height);
        projectRepository.save(image);
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


