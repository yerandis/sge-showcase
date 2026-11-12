
package com.yerandis.sge.service.serviceImpl.security;

import com.yerandis.sge.entity.admin.User;
import com.yerandis.sge.repository.admin.UserRepository;
import com.yerandis.sge.service.serviceInterface.security.ProfileAvatarAppService;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Map;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class ProfileAvatarService implements ProfileAvatarAppService {

    private final UserRepository userRepository;

    // Se obtiene de application.properties para no fijar la ruta en el código.
    @Value("${app.upload-dir:uploads/avatars}")
    private String uploadDir;

    @Override
    public String upload(String username, MultipartFile file) {
        if (file == null || file.isEmpty()) {
            throw new IllegalArgumentException("Debes seleccionar una imagen.");
        }

        // Limita el tamaño también en el servicio, además del límite HTTP.
        if (file.getSize() > 2 * 1024 * 1024) {
            throw new IllegalArgumentException("La imagen no puede superar 2 MB.");
        }

        // Solo se permiten formatos de imagen explícitamente admitidos.
        Map<String, String> extensions = Map.of(
                "image/jpeg", ".jpg",
                "image/png", ".png",
                "image/webp", ".webp"
        );

        String extension = extensions.get(file.getContentType());
        if (extension == null) {
            throw new IllegalArgumentException("Solo se admiten imágenes JPG, PNG o WebP.");
        }

        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new IllegalArgumentException("Usuario no encontrado."));

        String filename = UUID.randomUUID() + extension;
        Path directory = Paths.get(uploadDir).toAbsolutePath().normalize();
        Path destination = directory.resolve(filename).normalize();

        // Evita que una ruta manipulada pueda escribir fuera del directorio de avatares.
        if (!destination.startsWith(directory)) {
            throw new IllegalArgumentException("Ruta de imagen no válida.");
        }

        try {
            Files.createDirectories(directory);
            file.transferTo(destination);
        } catch (IOException e) {
            throw new IllegalStateException("No se pudo guardar la imagen.", e);
        }

        // Guardamos una ruta pública relativa, no la ruta física del servidor.
        user.setAvatarUrl("/uploads/avatars/" + filename);
        userRepository.save(user);

        return user.getAvatarUrl();
    }
}
