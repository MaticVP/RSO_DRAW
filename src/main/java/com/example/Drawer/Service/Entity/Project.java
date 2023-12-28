package com.example.Drawer.Service.Entity;

import jakarta.persistence.*;

@Entity
@Table(name = "\"project\"")
public class Project {

    @Id
    @GeneratedValue(strategy= GenerationType.AUTO)
    private Long id;

    public Long getUserId() {
        return userId;
    }

    public void setUserId(Long userId) {
        this.userId = userId;
    }

    private Long userId;

    private String projectName;

    private String image_description;

    private String dropbox_path;

    protected Project() {}

    public Project(String image_name, String image_description, int width, int height) {
        this.projectName = image_name;
        this.image_description = image_description;
    }

    public Project(String image_name, Long user_id, int width, int height) {
        this.projectName = image_name;
        this.userId = user_id;
        this.image_description = "";
    }

    @Override
    public String toString() {
        return String.format(
                "Project[id=%d, image_name='%s']", id, this.projectName);
    }


    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getImage_name() {
        return projectName;
    }

    public void setImage_name(String image_name) {
        this.projectName = image_name;
    }

    public String getImage_description() {
        return image_description;
    }

    public void setImage_description(String image_description) {
        this.image_description = image_description;
    }

}
