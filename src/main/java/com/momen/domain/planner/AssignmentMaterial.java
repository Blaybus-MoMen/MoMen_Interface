package com.momen.domain.planner;

import com.momen.core.entity.BaseTimeEntity;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Table(name = "assignment_materials")
public class AssignmentMaterial extends BaseTimeEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "material_id")
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "todo_id", nullable = false)
    private Todo todo;

    @Column(name = "file_url", nullable = false, length = 500)
    private String fileUrl;

    @Column(name = "file_name", nullable = false)
    private String fileName;

    public AssignmentMaterial(Todo todo, String fileUrl, String fileName) {
        this.todo = todo;
        this.fileUrl = fileUrl;
        this.fileName = fileName;
    }
}
