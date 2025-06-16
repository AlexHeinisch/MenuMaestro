import {ConfigurationParameters} from "../../generated/meals";

export class Configuration {
    basePath?: string;
    constructor(configurationParameters: ConfigurationParameters = {}) {
        this.basePath = configurationParameters.basePath;
    }
}
