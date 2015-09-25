package com.baverty.webcompiler.repositories;

import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

import com.baverty.webcompiler.domain.Execution;

@Repository
public interface ExecutionsRepository extends CrudRepository<Execution, Long>{

}
