package com.aka.bestprice.web.rest;

import com.codahale.metrics.annotation.Timed;
import com.aka.bestprice.domain.Seller;
import com.aka.bestprice.repository.SellerRepository;
import com.aka.bestprice.repository.search.SellerSearchRepository;
import com.aka.bestprice.web.rest.util.PaginationUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import javax.inject.Inject;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

import static org.elasticsearch.index.query.QueryBuilders.*;

/**
 * REST controller for managing Seller.
 */
@RestController
@RequestMapping("/api")
public class SellerResource {

    private final Logger log = LoggerFactory.getLogger(SellerResource.class);

    @Inject
    private SellerRepository sellerRepository;

    @Inject
    private SellerSearchRepository sellerSearchRepository;

    /**
     * POST  /sellers -> Create a new seller.
     */
    @RequestMapping(value = "/sellers",
            method = RequestMethod.POST,
            produces = MediaType.APPLICATION_JSON_VALUE)
    @Timed
    public ResponseEntity<Void> create(@RequestBody Seller seller) throws URISyntaxException {
        log.debug("REST request to save Seller : {}", seller);
        if (seller.getId() != null) {
            return ResponseEntity.badRequest().header("Failure", "A new seller cannot already have an ID").build();
        }
        sellerRepository.save(seller);
        sellerSearchRepository.save(seller);
        return ResponseEntity.created(new URI("/api/sellers/" + seller.getId())).build();
    }

    /**
     * PUT  /sellers -> Updates an existing seller.
     */
    @RequestMapping(value = "/sellers",
        method = RequestMethod.PUT,
        produces = MediaType.APPLICATION_JSON_VALUE)
    @Timed
    public ResponseEntity<Void> update(@RequestBody Seller seller) throws URISyntaxException {
        log.debug("REST request to update Seller : {}", seller);
        if (seller.getId() == null) {
            return create(seller);
        }
        sellerRepository.save(seller);
        sellerSearchRepository.save(seller);
        return ResponseEntity.ok().build();
    }

    /**
     * GET  /sellers -> get all the sellers.
     */
    @RequestMapping(value = "/sellers",
            method = RequestMethod.GET,
            produces = MediaType.APPLICATION_JSON_VALUE)
    @Timed
    public ResponseEntity<List<Seller>> getAll(@RequestParam(value = "page" , required = false) Integer offset,
                                  @RequestParam(value = "per_page", required = false) Integer limit)
        throws URISyntaxException {
        Page<Seller> page = sellerRepository.findAll(PaginationUtil.generatePageRequest(offset, limit));
        HttpHeaders headers = PaginationUtil.generatePaginationHttpHeaders(page, "/api/sellers", offset, limit);
        return new ResponseEntity<>(page.getContent(), headers, HttpStatus.OK);
    }

    /**
     * GET  /sellers/:id -> get the "id" seller.
     */
    @RequestMapping(value = "/sellers/{id}",
            method = RequestMethod.GET,
            produces = MediaType.APPLICATION_JSON_VALUE)
    @Timed
    public ResponseEntity<Seller> get(@PathVariable Long id) {
        log.debug("REST request to get Seller : {}", id);
        return Optional.ofNullable(sellerRepository.findOne(id))
            .map(seller -> new ResponseEntity<>(
                seller,
                HttpStatus.OK))
            .orElse(new ResponseEntity<>(HttpStatus.NOT_FOUND));
    }

    /**
     * DELETE  /sellers/:id -> delete the "id" seller.
     */
    @RequestMapping(value = "/sellers/{id}",
            method = RequestMethod.DELETE,
            produces = MediaType.APPLICATION_JSON_VALUE)
    @Timed
    public void delete(@PathVariable Long id) {
        log.debug("REST request to delete Seller : {}", id);
        sellerRepository.delete(id);
        sellerSearchRepository.delete(id);
    }

    /**
     * SEARCH  /_search/sellers/:query -> search for the seller corresponding
     * to the query.
     */
    @RequestMapping(value = "/_search/sellers/{query}",
        method = RequestMethod.GET,
        produces = MediaType.APPLICATION_JSON_VALUE)
    @Timed
    public List<Seller> search(@PathVariable String query) {
        return StreamSupport
            .stream(sellerSearchRepository.search(queryString(query)).spliterator(), false)
            .collect(Collectors.toList());
    }
}
