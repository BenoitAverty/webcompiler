package com.baverty.webcompiler.repositories;

import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

import com.baverty.webcompiler.domain.Program;

@Repository
public interface ProgramsRepository extends CrudRepository<Program, Long> {
	
}
