package io.metadew.iesi.server.rest.resource.script.resource;


import io.metadew.iesi.metadata.definition.Script;
import io.metadew.iesi.server.rest.controller.ScriptController;
import io.metadew.iesi.server.rest.resource.script.dto.ScriptDto;
import org.modelmapper.ModelMapper;
import org.springframework.hateoas.mvc.ResourceAssemblerSupport;
import org.springframework.stereotype.Component;

import java.util.List;

import static org.springframework.hateoas.mvc.ControllerLinkBuilder.linkTo;
import static org.springframework.hateoas.mvc.ControllerLinkBuilder.methodOn;

@Component
public class ScriptDtoResourceAssembler extends ResourceAssemblerSupport<List<Script>, ScriptDto> {

    private final ModelMapper modelMapper;

    public ScriptDtoResourceAssembler() {
        super(ScriptController.class, ScriptDto.class);
        this.modelMapper = new ModelMapper();
    }

    @Override
    public ScriptDto toResource(List<Script> scripts) {
        ScriptDto scriptByNameDto = convertToDto(scripts);
        scriptByNameDto.add(linkTo(methodOn(ScriptController.class)
                .getByNameScript(scriptByNameDto.getName()))
                .withSelfRel());
        return scriptByNameDto;
    }

    private ScriptDto convertToDto(List<Script> scripts) {


        ScriptDto connectionByNameDto = modelMapper.map(scripts.get(0), ScriptDto.class);
//
        return connectionByNameDto;
    }
}