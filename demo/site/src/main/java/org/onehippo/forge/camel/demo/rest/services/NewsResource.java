/*
 * Copyright 2014-2014 Hippo B.V. (http://www.onehippo.com)
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *         http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.onehippo.forge.camel.demo.rest.services;

import javax.jcr.ItemNotFoundException;
import javax.jcr.Node;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriInfo;

import org.apache.commons.lang.StringUtils;
import org.hippoecm.hst.container.RequestContextProvider;
import org.hippoecm.hst.core.request.HstRequestContext;
import org.hippoecm.hst.jaxrs.services.AbstractResource;
import org.onehippo.forge.camel.demo.beans.NewsDocument;
import org.onehippo.forge.camel.demo.rest.beans.NewsRepresentation;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Path("/news/")
public class NewsResource extends AbstractResource {

    private static Logger log = LoggerFactory.getLogger(NewsResource.class);

    @GET
    @Path("/id/{identifier}/")
    public NewsRepresentation getNewsArticle(@Context HttpServletRequest servletRequest, @Context HttpServletResponse servletResponse, @Context UriInfo uriInfo,
        @PathParam("identifier") String identifier) {

        NewsRepresentation newsRep = null;

        if (StringUtils.isNotEmpty(identifier)) {
            try {
                HstRequestContext requestContext = RequestContextProvider.get();
                Node node = requestContext.getSession().getNodeByIdentifier(identifier);
                NewsDocument newsBean = (NewsDocument) getObjectConverter(requestContext).getObject(node);
                newsRep = new NewsRepresentation().represent(newsBean);
            } catch (ItemNotFoundException e) {
                log.warn("The news is not found by the identifier: '{}'", identifier);
            } catch (Exception e) {
                if (log.isDebugEnabled()) {
                    log.warn("Failed to find news by identifier.", e);
                } else {
                    log.warn("Failed to find news by identifier. {}", e.toString());
                }

                throw new WebApplicationException(e, buildServerErrorResponse(e));
            }
        }

        if (newsRep == null) {
            throw new WebApplicationException(Response.Status.NOT_FOUND.getStatusCode());
        }

        return newsRep;
    }

    private static Response buildServerErrorResponse(Throwable th) {
        return Response.serverError().entity(th.getCause() != null ? th.getCause().toString() : th.toString()).build();
    }
}
