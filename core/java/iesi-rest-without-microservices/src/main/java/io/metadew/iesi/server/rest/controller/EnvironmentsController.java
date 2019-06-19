package io.metadew.iesi.server.rest.controller;

import io.metadew.iesi.metadata.configuration.ConnectionConfiguration;
import io.metadew.iesi.metadata.configuration.EnvironmentConfiguration;
import io.metadew.iesi.metadata.configuration.exception.EnvironmentAlreadyExistsException;
import io.metadew.iesi.metadata.configuration.exception.EnvironmentDoesNotExistException;
import io.metadew.iesi.metadata.definition.Connection;
import io.metadew.iesi.metadata.definition.Environment;

import io.metadew.iesi.server.rest.error.*;

import io.metadew.iesi.server.rest.error.DataNotFoundException;
import io.metadew.iesi.server.rest.error.GetListNullProperties;
import io.metadew.iesi.server.rest.error.GetNullProperties;

import io.metadew.iesi.server.rest.pagination.EnvironmentCriteria;
import io.metadew.iesi.server.rest.pagination.EnvironmentPagination;
import io.metadew.iesi.server.rest.resource.HalMultipleEmbeddedResource;
import io.metadew.iesi.server.rest.resource.connection.dto.ConnectionDto;
import io.metadew.iesi.server.rest.resource.connection.resource.ConnectionDtoResourceAssembler;
import io.metadew.iesi.server.rest.resource.environment.dto.EnvironmentDto;
import io.metadew.iesi.server.rest.resource.environment.resource.EnvironmentDtoResourceAssembler;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import javax.validation.Valid;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import static io.metadew.iesi.server.rest.helper.Filter.distinctByKey;
import static org.springframework.hateoas.mvc.ControllerLinkBuilder.linkTo;
import static org.springframework.hateoas.mvc.ControllerLinkBuilder.methodOn;

@RestController
public class EnvironmentsController {

	private EnvironmentConfiguration environmentConfiguration;

	private ConnectionConfiguration connectionConfiguration;

	private final GetNullProperties getNullProperties;

	private final GetListNullProperties getListNullProperties;

	private final EnvironmentPagination environmentPagination;

	@Autowired
	private EnvironmentDtoResourceAssembler environmentDtoResourceAssembler;

	@Autowired
	private ConnectionDtoResourceAssembler connectionDtoResourceAssembler;

	@Autowired
	EnvironmentsController(EnvironmentConfiguration environmentConfiguration, GetNullProperties getNullProperties, GetListNullProperties getListNullProperties, ConnectionConfiguration connectionConfiguration,
						   EnvironmentPagination environmentPagination) {
		this.environmentConfiguration = environmentConfiguration;
		this.connectionConfiguration = connectionConfiguration;
		this.environmentPagination = environmentPagination;
		this.getListNullProperties = getListNullProperties;
		this.getNullProperties = getNullProperties;
	}

	@GetMapping("/environments")
	public HalMultipleEmbeddedResource<EnvironmentDto> getAllEnvironments(@Valid EnvironmentCriteria environmentCriteria) {
		List<Environment> environments = environmentConfiguration.getAllEnvironments();
		List<Environment> pagination = environmentPagination.search(environments, environmentCriteria);
		return new HalMultipleEmbeddedResource<EnvironmentDto>(
				pagination.stream().filter(distinctByKey(Environment::getName))
						.map(environment -> environmentDtoResourceAssembler.toResource(environment))
						.collect(Collectors.toList()));
	}

	@GetMapping("/environments/{name}")
	public EnvironmentDto getByName(@PathVariable String name) {
		return environmentConfiguration.getEnvironment(name)
				.map(environment -> environmentDtoResourceAssembler.toResource(environment))
				.orElseThrow(() -> new DataNotFoundException(name));
	}

	//
	@PostMapping("/environments")
	public EnvironmentDto postAllEnvironments(@Valid @RequestBody EnvironmentDto environment) {
		getNullProperties.getNullEnvironment(environment);
		try {
			// TODO: make insert return environment
			environmentConfiguration.insertEnvironment(environment.convertToEntity());
			return environmentDtoResourceAssembler.toResource(environment.convertToEntity());
		} catch (EnvironmentAlreadyExistsException e) {
			e.printStackTrace();
			throw new ResponseStatusException(HttpStatus.NOT_FOUND,
					"Environment " + environment.getName() + " already exists");
		}
	}

	@PutMapping("/environments")
	public HalMultipleEmbeddedResource<EnvironmentDto> putAllConnections(@Valid @RequestBody List<EnvironmentDto> environmentDtos) {
		HalMultipleEmbeddedResource<EnvironmentDto> halMultipleEmbeddedResource = new HalMultipleEmbeddedResource<>();
		getListNullProperties.getNullEnvironment(environmentDtos);
		for (EnvironmentDto environmentDto : environmentDtos) {
			try {
				environmentConfiguration.updateEnvironment(environmentDto.convertToEntity());
				halMultipleEmbeddedResource.embedResource(environmentDto);
				halMultipleEmbeddedResource.add(linkTo(methodOn(EnvironmentsController.class)
						.getByName(environmentDto.getName()))
						.withRel(environmentDto.getName()));
			} catch (EnvironmentDoesNotExistException e) {
				e.printStackTrace();
				throw new DataNotFoundException(environmentDto.getName());
			}
		}

		return halMultipleEmbeddedResource;
	}

	@PutMapping("/environments/{name}")
	public EnvironmentDto putEnvironments(@PathVariable String name,
										  @RequestBody EnvironmentDto environment) {
	getNullProperties.getNullEnvironment(environment);
		if (!environment.getName().equals(name)) {
			throw new DataBadRequestException(name);
		} else if (environment.getName() == null){
			throw new DataNotFoundException(name);
		}
		try {
			// TODO: make update return Environment
			environmentConfiguration.updateEnvironment(environment.convertToEntity());
			return environmentDtoResourceAssembler.toResource(environment.convertToEntity());
		} catch (EnvironmentDoesNotExistException e) {
			throw new DataNotFoundException(name);
		}

	}

	@GetMapping("/environments/{name}/connections")
	public HalMultipleEmbeddedResource getEnvironmentsConnections(@PathVariable String name) {
		List<Connection> connections = connectionConfiguration.getConnections();
		List<Connection> result = connections.stream().filter(connection -> connection.getEnvironment().equals(name))
				.collect(Collectors.toList());
		if (result.isEmpty()) {
			throw new DataNotFoundException(name);
		}
		HalMultipleEmbeddedResource<ConnectionDto> halMultipleEmbeddedResource = new HalMultipleEmbeddedResource<>(result.stream().map(connectionDtoResourceAssembler::toResource).collect(Collectors.toList()));
		return halMultipleEmbeddedResource;

	}

	@DeleteMapping("/environments")
	public ResponseEntity<?> deleteAllEnvironments() {
		List<Environment> environment = environmentConfiguration.getAllEnvironments();
		if (!environment.isEmpty()) {
			environmentConfiguration.deleteAllEnvironments();
			return ResponseEntity.status(HttpStatus.OK).build();
		}
		return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
	}

	@DeleteMapping("environments/{name}")
	public ResponseEntity<?> deleteEnvironments(@PathVariable String name) {
		Optional<Environment> environment = environmentConfiguration.getEnvironment(name);
		if (!environment.isPresent()) {
			throw new DataNotFoundException(name);
		}
		environmentConfiguration.deleteEnvironment(name);
		return ResponseEntity.status(HttpStatus.OK).build();
	}
}