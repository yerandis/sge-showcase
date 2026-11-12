package com.yerandis.sge.service.serviceInterface.security;

import org.springframework.web.multipart.MultipartFile;

public interface ProfileAvatarAppService {

    String upload(String name, MultipartFile file);
}
