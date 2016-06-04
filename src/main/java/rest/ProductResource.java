package rest;

import domain.Comment;
import domain.Product;

import javax.ejb.Stateless;
import javax.persistence.EntityManager;
import javax.persistence.NoResultException;
import javax.persistence.PersistenceContext;
import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.util.List;
import java.util.stream.Collectors;

@Path("/products")
@Stateless
public class ProductResource {
    @PersistenceContext
    private EntityManager em;

    @GET
    @Produces(MediaType.APPLICATION_JSON)
    public List<Product> getProducts() {
        return em.createNamedQuery("product.all", Product.class).getResultList();
    }

    @GET
    @Path("/{id}")
    @Produces(MediaType.APPLICATION_JSON)
    public Response getProduct(@PathParam("id") Integer id) {
        Product product;
        try {
            product = em.createNamedQuery("product.id", Product.class)
                    .setParameter("productId", id)
                    .getSingleResult();
        } catch (NoResultException ex) {
            return Response.status(404).build();
        }
        return Response.ok(product).build();
    }

    @POST
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public Product addProduct(Product product) {
        em.persist(product);
        return product;
    }

    @PUT
    @Path("/{id}")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public Response updateProduct(@PathParam("id") Integer id, Product product) {
        try {
            em.createNamedQuery("product.id", Product.class)
                    .setParameter("productId", id)
                    .getSingleResult();
        } catch (NoResultException ex) {
            return Response.status(404).build();
        }

        product.setId(id);
        em.merge(product);
        return Response.ok(product).build();
    }

    @GET
    @Path("/{id}/comments")
    @Produces(MediaType.APPLICATION_JSON)
    public Response getComments(@PathParam("id") Integer id) {
        Product product;
        try {
            product = em.createNamedQuery("product.id", Product.class)
                    .setParameter("productId", id)
                    .getSingleResult();
        } catch (NoResultException ex) {
            return Response.status(404).build();
        }
        return Response.ok(product.getComments()).build();
    }

    @POST
    @Path("/{id}/comments")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public Response addComment(@PathParam("id") Integer id, Comment comment) {
        Product product;
        try {
            product = em.createNamedQuery("product.id", Product.class)
                    .setParameter("productId", id)
                    .getSingleResult();
        } catch (NoResultException ex) {
            return Response.status(404).build();
        }
        product.getComments().add(comment);
        comment.setProduct(product);
        em.persist(comment);

        return Response.ok(comment).build();
    }

    @DELETE
    @Path("/{id}/comments/{commentId}")
    public Response deleteComment(@PathParam("id") Integer id, @PathParam("commentId") Integer commentId) {
        Product product;
        try {
            product = em.createNamedQuery("product.id", Product.class)
                    .setParameter("productId", id)
                    .getSingleResult();
        } catch (NoResultException ex) {
            return Response.status(404).build();
        }
        List<Comment> comments = product.getComments();
        for (int i = 0; i < comments.size(); i++) {
            if (comments.get(i).getId().equals(commentId)) {
                comments.get(i).setProduct(null);
                comments.remove(i);
            }
        }
        em.merge(product);

        return Response.ok().build();
    }
}
