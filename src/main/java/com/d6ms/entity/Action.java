package com.d6ms.entity;

import com.d6ms.type.ActionType;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Lob;
import jakarta.persistence.Table;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = true)
@Entity
@Table(name = "DMS_ACTION")
public class Action extends BaseEntity {

	@Enumerated(EnumType.STRING)
	@Column(name = "TYPE_")
	private ActionType type;

	@Lob
	@Column(name = "TARGET")
	private String targetHierarchy;

	@Column(name = "NODE_ID")
	private String nodeId;

}
