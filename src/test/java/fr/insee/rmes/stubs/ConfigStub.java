package fr.insee.rmes.stubs;

import fr.insee.rmes.config.Config;

public class ConfigStub extends Config {

    public static final int FILE_MAX_LENGTH = 100;

    @Override
    public int getMaxFileNameLength() {
        return FILE_MAX_LENGTH;
    }
}
