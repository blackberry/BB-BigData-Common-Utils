/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.blackberry.bdp.common.versioned;

import com.fasterxml.jackson.annotation.JsonIgnore;

/**
 *
 * @author dariens
 */
abstract class PojoProtected1 {
	@JsonIgnore  abstract String getProtectedString();
}
