package com.d6ms.entity;

import java.util.HashSet;
import java.util.Set;

import com.d6ms.type.NodeType;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.JoinColumns;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = true)
@Entity
@Table(name = "DMS_NODE")
public class Node extends BaseEntity {

	@Column(name = "NAME")
	private String name;

	@Column(name = "BUSINESS_KEY")
	private String businessKey;

	@Column(name = "MASTER_ID")
	private String masterId;

	@Column(name = "MASTER_TYPE")
	private String masterType;

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumns(@JoinColumn(name = "PARENT_ID"))
	private Node parent;

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumns(@JoinColumn(name = "STORE_ID", nullable = false))
	private Store store;

	@Column(name = "CATEGORY")
	private String category;

	@Enumerated(EnumType.STRING)
	@Column(name = "TYPE")
	private NodeType type;

	@Column(name = "MD5")
	private String md5;

	@Column(name = "SIZE")
	private long size;

	@Column(name = "CONTENT_TYPE")
	private String contentType;

	@OneToMany(mappedBy = "node")
	private Set<Metadata> metadata = new HashSet<>();

	@OneToMany(mappedBy = "parent", fetch = FetchType.LAZY)
	private Set<Node> children = new HashSet<>();

	@OneToOne(fetch = FetchType.LAZY)
	@JoinColumns(@JoinColumn(name = "CONTENT_ID"))
	private NodeContent nodeContent;

}
