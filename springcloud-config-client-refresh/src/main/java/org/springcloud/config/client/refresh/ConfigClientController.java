package org.springcloud.config.client.refresh;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.cloud.context.config.annotation.RefreshScope;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RefreshScope 
public class ConfigClientController {
	@Value("${test}")
	private String profile;
	    
	@RequestMapping("/")
    public String home() {
        return this.profile;
    }
}
