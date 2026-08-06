package com.webdev.bloggingsystem.s3Stuff;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartFile;

@Controller
public class S3Controller {
    private final S3Service s3Service;

    public S3Controller(S3Service s3Service) {
        this.s3Service = s3Service;
    }

    @PostMapping("/admin/upload-image")
    public String uploadImage(@RequestParam("folder") String folder, @RequestParam("file") MultipartFile file, Model model) {
        String url = s3Service.uploadImage(folder, file);
        model.addAttribute("imgUrl", url);

        return "components/post-components :: img-url";
    }


}
