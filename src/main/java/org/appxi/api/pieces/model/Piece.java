package org.appxi.api.pieces.model;

import org.apache.solr.client.solrj.beans.Field;
import org.springframework.data.solr.core.mapping.Dynamic;
import org.springframework.data.solr.core.mapping.Indexed;
import org.springframework.data.solr.core.mapping.SolrDocument;

import javax.persistence.*;
import java.util.List;
import java.util.Map;

@Entity
@Table(name = "pieces")
@SolrDocument(collection = "pieces")
public class Piece {

    @javax.persistence.Id
    @org.springframework.data.annotation.Id
    @Indexed("id")
    private String id;

    @Indexed("project_s")
    private String project;

    @Indexed("path_s")
    private String path;

    @Indexed("type_s")
    private String type;

    @Indexed("name_s")
    private String name;

    @Indexed("description_s")
    @Column(name = "description", columnDefinition = "CLOB")
    private String description;

    @Field("categories_ss")
    @Lob
    @ElementCollection
    @CollectionTable(name = "categories", joinColumns = @JoinColumn(name = "cat_fk"))
    @Column(name = "v", columnDefinition = "CLOB")
    private List<String> categories;

    // <field name="mapentry[0].key">mapentry[0].value</field>
    // <field name="mapentry[1].key">mapentry[1].value</field>
    @Field("field_*")
    @Lob
    @ElementCollection
    @MapKeyColumn(name = "k")
    @Column(name = "v", columnDefinition = "CLOB")
    @CollectionTable(name = "fields", joinColumns = @JoinColumn(name = "fld_fk"))
    private Map<String, String> fields;

    // <field name="'text_' + mapentry[0].key">mapentry[0].value</field>
    // <field name="'text_' + mapentry[1].key">mapentry[1].value</field>
    @Dynamic
    @Field("text_*")
    @Lob
    @ElementCollection
    @MapKeyColumn(name = "k")
    @Column(name = "v", columnDefinition = "CLOB")
    @CollectionTable(name = "texts", joinColumns = @JoinColumn(name = "txt_fk"))
    private Map<String, String> texts;

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getProject() {
        return project;
    }

    public void setProject(String project) {
        this.project = project;
    }

    public String getPath() {
        return path;
    }

    public void setPath(String path) {
        this.path = path;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public List<String> getCategories() {
        return categories;
    }

    public void setCategories(List<String> categories) {
        this.categories = categories;
    }

    public Map<String, String> getFields() {
        return fields;
    }

    public void setFields(Map<String, String> fields) {
        this.fields = fields;
    }

    public Map<String, String> getTexts() {
        return texts;
    }

    public void setTexts(Map<String, String> texts) {
        this.texts = texts;
    }
}
	
