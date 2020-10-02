package io.metadew.iesi.metadata.definition;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import io.metadew.iesi.metadata.definition.key.MetadataKey;
import io.metadew.iesi.metadata.definition.security.SecurityGroupKey;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;

@JsonDeserialize(using = MetadataJsonComponent.Deserializer.class)
@Data
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
public abstract class SecuredObject<T extends MetadataKey> extends Metadata<T> {

    private final SecurityGroupKey securityGroupKey;

    public SecuredObject(T metadataKey, SecurityGroupKey securityGroupKey) {
        super(metadataKey);
        this.securityGroupKey = securityGroupKey;
    }

}
