package com.baverty.webcompiler.repositories;

import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

import com.baverty.webcompiler.domain.Program;

@Repository
public interface ProgramRepository extends CrudRepository<Program, Long> {
	
	/**
	 * Finds the program with the given tid.
	 * @param tid
	 * @return the program with the given tid
	 */
	Program findByTid(Long tid);
	
}
