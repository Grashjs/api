package com.grash.model;

import com.grash.model.enums.Priority;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.persistence.*;
import java.util.Collection;
import java.util.Date;

@Entity
@Data
@NoArgsConstructor
public class Request {
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Long id;

    private String title;

    private String description;

    private Priority priority = Priority.NONE;

    @ManyToMany
    @JoinTable( name = "T_Request_File_Associations",
            joinColumns = @JoinColumn( name = "idRequest" ),
            inverseJoinColumns = @JoinColumn( name = "idFile" ) )
    private Collection<File> files;
    @OneToOne
    private Image image;

    private Long createdBy;

//  optionalfields;

    @OneToOne
    private Asset asset;

    @OneToOne
    private Location location;

    @OneToOne
    private User assignedTo;

    private Date dueDate;

    private String category;

    @OneToOne
    private Team team;

}
