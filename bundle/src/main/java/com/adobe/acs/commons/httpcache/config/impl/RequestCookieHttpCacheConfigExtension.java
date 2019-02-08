/*
 * #%L
 * ACS AEM Commons Bundle
 * %%
 * Copyright (C) 2015 Adobe
 * %%
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * #L%
 */
package com.adobe.acs.commons.httpcache.config.impl;

import com.adobe.acs.commons.httpcache.config.HttpCacheConfigExtension;
import com.adobe.acs.commons.httpcache.config.impl.keys.helper.KeyValueConfigHelper;
import com.adobe.acs.commons.httpcache.config.impl.keys.helper.KeyValueMapWrapperBuilder;
import com.adobe.acs.commons.httpcache.config.impl.keys.helper.RequestCookieKeyValueWrapperBuilder;
import com.adobe.acs.commons.httpcache.keys.CacheKeyFactory;
import com.google.common.collect.ImmutableSet;
import org.apache.commons.lang3.StringUtils;
import org.apache.sling.api.SlingHttpServletRequest;
import org.apache.sling.spi.resource.provider.ResourceProvider;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.ConfigurationPolicy;
import org.osgi.service.component.annotations.Modified;
import org.osgi.service.metatype.annotations.AttributeDefinition;
import org.osgi.service.metatype.annotations.Designate;
import org.osgi.service.metatype.annotations.ObjectClassDefinition;

import javax.servlet.http.Cookie;
import java.util.Map;
import java.util.Set;

/**
 * RequestCookieHttpCacheConfigExtension
 * <p>
 * This extension on the HTTP cache allows for specific cookie combinations to create seperated cache entries.
 * This so we can present a different header based on cookie values, which tell us if a user is logged in and what type of user it is.
 * </p>
 *
 */
@Component(configurationPolicy = ConfigurationPolicy.REQUIRE, service = {HttpCacheConfigExtension.class, CacheKeyFactory.class})
@Designate(ocd = RequestCookieHttpCacheConfigExtension.Config.class, factory = true)
public class RequestCookieHttpCacheConfigExtension extends AbstractKeyValueExtension implements HttpCacheConfigExtension, CacheKeyFactory {

    @ObjectClassDefinition(name = "ACS AEM Commons - HTTP Cache - Request cookie based extension for HttpCacheConfig and CacheKeyFactory",
        description = "Defined key / values that will be allowed for this extension.")
    public @interface Config {
        @AttributeDefinition(
            name = "Allowed keys",
            description = "ValueMap keys that will used to generate a cache key."
        )
        String[] allowedKeys() default {};

        @AttributeDefinition(
            name = "Allowed values",
            description = "If set, narrows down specified keys to specified values only."
        )
        String[] allowedValues() default {};

        @AttributeDefinition(
            name = "Empty is allowed",
            description = "Allows no value match to be a cache entry."
        )
        boolean emptyAllowed() default false;

        @AttributeDefinition(name = "Config Name")
        String configName() default StringUtils.EMPTY;

        @AttributeDefinition
        String webconsole_configurationFactory_nameHint() default "Configuration: Keys ({allowedKeys}), Values ({allowedValues})";
    }


    public static final String KEY_STRING_REPRENSENTATION = "CookieKeyValues";

    @Override
    protected String getKeyToStringRepresentation() {
        return KEY_STRING_REPRENSENTATION;
    }

    @Override
    protected KeyValueMapWrapperBuilder getBuilder(SlingHttpServletRequest request, Set<String> allowedKeys, Map<String, String> allowedValues) {
        ImmutableSet<Cookie> presentCookies = ImmutableSet.copyOf(request.getCookies());
        return new RequestCookieKeyValueWrapperBuilder(allowedKeys, allowedValues, presentCookies);
    }

    @Activate
    @Modified
    protected void activate(Config config){
        this.emptyAllowed = config.emptyAllowed();
        this.valueMapKeys = ImmutableSet.copyOf(config.allowedKeys());
        this.configName = config.configName();
        this.allowedValues = KeyValueConfigHelper.convertAllowedValues(config.allowedValues());
    }

}