package org.dbp.conf;

import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;

import org.dbp.conf.aop.log.LogAspect;
import org.dbp.conf.interceptor.LogInterceptor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.EnableAspectJAutoProxy;
import org.springframework.context.annotation.Import;
import org.springframework.core.env.Environment;
import org.springframework.http.converter.HttpMessageConverter;
import org.springframework.http.converter.json.Jackson2ObjectMapperBuilder;
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter;
import org.springframework.http.converter.xml.MappingJackson2XmlHttpMessageConverter;
import org.springframework.security.access.SecurityConfig;
import org.springframework.web.servlet.config.annotation.EnableWebMvc;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurerAdapter;
import org.springframework.web.servlet.resource.PathResourceResolver;
import org.springframework.web.servlet.view.InternalResourceViewResolver;

import com.fasterxml.jackson.databind.module.SimpleModule;
import com.fasterxml.jackson.datatype.jsr310.PackageVersion;
import com.fasterxml.jackson.datatype.jsr310.deser.LocalDateDeserializer;
import com.fasterxml.jackson.datatype.jsr310.ser.LocalDateSerializer;

@Configuration
@EnableWebMvc
@EnableAspectJAutoProxy
@ComponentScan(
		basePackages={
				"org.dbp.controller"	// Es donde se ubicaran los controladores
				}
		)
@Import(SeguridadConfig.class) 
public class WebConfig extends WebMvcConfigurerAdapter{

	@Autowired private Environment env;
	
	@Bean
	public LogAspect logAspect(){
		return new LogAspect();
	}
	
	/**
	 * 
	 * Configuramos que las paginas se guarden en la carpeta pages
	 * 
	 * @return
	 */
    @Bean
    public InternalResourceViewResolver getInternalResourceViewResolver() {
        InternalResourceViewResolver resolver = new InternalResourceViewResolver();
        resolver.setPrefix("/WEB-INF/pages/");
        resolver.setSuffix(".jsp");
        return resolver;
    }

	@Override
	public void addInterceptors(InterceptorRegistry registry) {
		super.addInterceptors(registry);
		registry.addInterceptor(new LogInterceptor());
	}
	
	
	@Override
	public void addResourceHandlers(ResourceHandlerRegistry registry) {
		String entorno=env.getActiveProfiles()[0];
		registry
	      .addResourceHandler("/resources/**")
	      .addResourceLocations("/resources/")
	      .setCachePeriod(3600)
	      .resourceChain(true)
	      .addResolver(new PathResourceResolver());
		registry
	      .addResourceHandler("/env/**")
	      .addResourceLocations("/resources/env/"+entorno+"/")
	      .setCachePeriod(3600)
	      .resourceChain(true)
	      .addResolver(new PathResourceResolver());		
	}



	private  class LocalDateDeserializerEs  extends LocalDateDeserializer{
		private LocalDateDeserializerEs(){
			super(DateTimeFormatter.ofPattern("dd/MM/yyyy"));
		}
	}
	
	private class LocalDateSerializerEs extends LocalDateSerializer{
		private  LocalDateSerializerEs(){
			super(false,DateTimeFormatter.ofPattern("dd/MM/yyyy"));
		}
	}
	
	private class JSR310ModuleEs extends SimpleModule{
		public JSR310ModuleEs() {
			super(PackageVersion.VERSION);
	        addDeserializer(LocalDate.class,new LocalDateDeserializerEs());
	        addSerializer(LocalDate.class, new LocalDateSerializerEs());
		}
	}

	@Override
	public void configureMessageConverters(
			List<HttpMessageConverter<?>> converters) {
		super.configureMessageConverters(converters);
		Jackson2ObjectMapperBuilder builder = new Jackson2ObjectMapperBuilder();
	    builder
	    	.indentOutput(true)
	    	.dateFormat(new SimpleDateFormat("dd-MM-yyyy")
	    	);
	    converters.add(new MappingJackson2HttpMessageConverter(
	    				builder
	    					.build()
	    					.registerModule(new JSR310ModuleEs())));
	    converters.add(new MappingJackson2XmlHttpMessageConverter(
	    				builder.createXmlMapper(true)
	    					.build()
	    					.registerModule(new JSR310ModuleEs())));
	}
    
 
    
}
