package org.jsondoc.core.util;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import javax.servlet.ServletContext;

import org.jsondoc.core.annotation.Api;
import org.jsondoc.core.annotation.ApiBodyObject;
import org.jsondoc.core.annotation.ApiErrors;
import org.jsondoc.core.annotation.ApiHeaders;
import org.jsondoc.core.annotation.ApiMethod;
import org.jsondoc.core.annotation.ApiObject;
import org.jsondoc.core.annotation.ApiParams;
import org.jsondoc.core.annotation.ApiResponseObject;
import org.jsondoc.core.pojo.ApiBodyObjectDoc;
import org.jsondoc.core.pojo.ApiDoc;
import org.jsondoc.core.pojo.ApiErrorDoc;
import org.jsondoc.core.pojo.ApiHeaderDoc;
import org.jsondoc.core.pojo.ApiMethodDoc;
import org.jsondoc.core.pojo.ApiObjectDoc;
import org.jsondoc.core.pojo.ApiParamDoc;
import org.jsondoc.core.pojo.ApiResponseObjectDoc;
import org.jsondoc.core.pojo.JSONDoc;
import org.reflections.Reflections;
import org.reflections.util.ClasspathHelper;
import org.reflections.util.ConfigurationBuilder;

public class JSONDocUtils {
	private static Reflections reflections = null;
	
	/**
	 * Returns the main <code>ApiDoc</code>, containing <code>ApiMethodDoc</code> and <code>ApiObjectDoc</code> objects
	 * @return An <code>ApiDoc</code> object
	 */
	public static JSONDoc getApiDoc(ServletContext servletContext, String version, String basePath) {
		reflections = new Reflections(new ConfigurationBuilder().setUrls(ClasspathHelper.forWebInfClasses(servletContext)));
		JSONDoc apiDoc = new JSONDoc(version, basePath);
		apiDoc.setApis(getApiDocs(reflections.getTypesAnnotatedWith(Api.class)));
		apiDoc.setObjects(getApiObjectDocs(reflections.getTypesAnnotatedWith(ApiObject.class)));
		return apiDoc;
	}
	
	public static List<ApiDoc> getApiDocs(Set<Class<?>> classes) {
		List<ApiDoc> apiDocs = new ArrayList<ApiDoc>();
		for (Class<?> controller : classes) {
			ApiDoc apiDoc = ApiDoc.buildFromAnnotation(controller.getAnnotation(Api.class));
			apiDoc.setMethods(getApiMethodDocs(controller));
			apiDocs.add(apiDoc);
		}
		return apiDocs;
	}
	
	public static List<ApiObjectDoc> getApiObjectDocs(Set<Class<?>> classes) {
		List<ApiObjectDoc> pojoDocs = new ArrayList<ApiObjectDoc>();
		for (Class<?> pojo : classes) {
			ApiObject annotation = pojo.getAnnotation(ApiObject.class);
			ApiObjectDoc pojoDoc = ApiObjectDoc.buildFromAnnotation(annotation, pojo);
			if(annotation.show()) {
				pojoDocs.add(pojoDoc);
			}
		}
		return pojoDocs;
	}
	
	private static List<ApiMethodDoc> getApiMethodDocs(Class<?> controller) {
		List<ApiMethodDoc> apiMethodDocs = new ArrayList<ApiMethodDoc>();
		Method[] methods = controller.getMethods();
		for (Method method : methods) {
			if(method.isAnnotationPresent(ApiMethod.class)) {
				ApiMethodDoc apiMethodDoc = ApiMethodDoc.buildFromAnnotation(method.getAnnotation(ApiMethod.class));
				
				if(method.isAnnotationPresent(ApiHeaders.class)) {
					apiMethodDoc.setHeaders(ApiHeaderDoc.buildFromAnnotation(method.getAnnotation(ApiHeaders.class)));
				}
				
				if(method.isAnnotationPresent(ApiParams.class)) {
					apiMethodDoc.setUrlparameters(ApiParamDoc.buildFromAnnotation(method.getAnnotation(ApiParams.class)));
				}
				
				if(method.isAnnotationPresent(ApiBodyObject.class)) {
					apiMethodDoc.setBodyobject(ApiBodyObjectDoc.buildFromAnnotation(method.getAnnotation(ApiBodyObject.class)));
				}
				
				if(method.isAnnotationPresent(ApiResponseObject.class)) {
					apiMethodDoc.setResponse(ApiResponseObjectDoc.buildFromAnnotation(method.getAnnotation(ApiResponseObject.class)));
				}
				
				if(method.isAnnotationPresent(ApiErrors.class)) {
					apiMethodDoc.setApierrors(ApiErrorDoc.buildFromAnnotation(method.getAnnotation(ApiErrors.class)));
				}
				
				apiMethodDocs.add(apiMethodDoc);
			}
			
		}
		return apiMethodDocs;
	}
	
}
