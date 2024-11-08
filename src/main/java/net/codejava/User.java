// con esta clase podemos almacenar y obtener informacion de los usuarios en la base de datos
package net.codejava;

import javax.persistence.*;

@Entity
@Table(name = "users")
public class User {

    @Id //declaraciones
    @GeneratedValue(strategy = GenerationType.IDENTITY) // la base de datos genera automaticamente este valor
    private Long id;

    @Column(nullable = false, unique = true, length = 45)
    private String email;

    @Column(nullable = false, length = 64)
    private String password;

    @Column(name = "first_name", nullable = false, length = 20)
    private String firstName;

    @Column(name = "last_name", nullable = false, length = 20)
    private String lastName;

    @Column(nullable = false)
    private boolean enabled;

    @Column(nullable = false, length = 10)
    private String role;

    public User() {
        this.role = "USER"; // USER como default
        this.enabled = true;  // esta habilitado por default
    }

    @Lob // indica que es un campo de gran tamaño
    @Column(name = "photo")
    private byte[] photo; // almacena la imagen en binario

    @Transient
    private String photoBase64; // Campo temporal para base64

    // Getters y setters de aqui hasta abajo

    public byte[] getPhoto() {
        return photo;
    }

    public void setPhoto(byte[] photo) {
        this.photo = photo;
    }

    public String getPhotoBase64() {
        return photoBase64;
    }

    public void setPhotoBase64(String photoBase64) {
        this.photoBase64 = photoBase64;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getFirstName() {
        return firstName;
    }

    public void setFirstName(String firstName) {
        this.firstName = firstName;
    }

    public String getLastName() {
        return lastName;
    }

    public void setLastName(String lastName) {
        this.lastName = lastName;
    }

    public boolean isEnabled() {
        return enabled;
    }

    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }

    public String getRole() {
        return role;
    }

    public void setRole(String role) {
        this.role = role;
    }


}
