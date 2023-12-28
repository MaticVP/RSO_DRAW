package com.example.Drawer.Service.Services;

import com.dropbox.core.v2.DbxClientV2;
import com.dropbox.core.v2.files.FileMetadata;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.ByteArrayOutputStream;
import java.io.InputStream;

@Service
public class DropboxService {

    @Autowired
    private DbxClientV2 dropboxClient;

    public void uploadFile(InputStream fileInputStream, String fileName, String user_name) {
        try
        {
            dropboxClient.files().createFolderV2("/"+user_name);
        }
        catch (Exception f) {
            try (InputStream in = fileInputStream) {
                if (dropboxClient != null) {
                    FileMetadata metadata = dropboxClient.files().uploadBuilder("/" + user_name + "/" + user_name+".png")
                            .uploadAndFinish(in);
                }
                // Handle metadata or any additional logic
            } catch(Exception e) {
                e.printStackTrace();
            }
        }
    }

    public byte[] getImage(String user_name,String project_name) {
        try
        {
            dropboxClient.files().createFolderV2("/"+user_name);
        }
        catch (Exception f) {
            try (InputStream inputStream = dropboxClient.files().downloadBuilder("/"+user_name+"/"+project_name).start().getInputStream()) {
                if (dropboxClient != null) {
                    ByteArrayOutputStream out = new ByteArrayOutputStream();
                    byte[] buffer = new byte[1024];
                    int bytesRead;
                    while ((bytesRead = inputStream.read(buffer)) != -1) {
                        out.write(buffer, 0, bytesRead);
                    }
                    return out.toByteArray();
                }
                // Handle metadata or any additional logic
            } catch(Exception e) {
                e.printStackTrace();
            }
        }
        return new byte[0];
    }

    public void saveImage(InputStream fileInputStream, String project_name, String user_name) {
        try
        {
            dropboxClient.files().createFolderV2("/"+user_name);
        }
        catch (Exception f) {
            try (InputStream in = fileInputStream) {
                if (dropboxClient != null) {
                    FileMetadata metadata = dropboxClient.files().uploadBuilder("/" + user_name + "/" + project_name+".png")
                            .uploadAndFinish(in);
                }
                // Handle metadata or any additional logic
            } catch(Exception e) {
                e.printStackTrace();
            }
        }
    }

    public void createFolder(InputStream fileInputStream, String user_profile_path) {
        try (InputStream in = fileInputStream) {
            if (dropboxClient != null) {
                dropboxClient.files().createFolderV2("/"+user_profile_path);
            }
            // Handle metadata or any additional logic
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    // Add more methods for other Dropbox API functionalities as needed
}