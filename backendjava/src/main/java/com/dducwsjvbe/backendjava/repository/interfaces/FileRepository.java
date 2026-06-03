package com.dducwsjvbe.backendjava.repository.interfaces;

import com.dducwsjvbe.backendjava.model.File;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface FileRepository extends JpaRepository<File,Long> {
}
