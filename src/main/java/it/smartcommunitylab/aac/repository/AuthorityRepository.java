/**
 *    Copyright 2015-2019 Smart Community Lab, FBK
 *
 *    Licensed under the Apache License, Version 2.0 (the "License");
 *    you may not use this file except in compliance with the License.
 *    You may obtain a copy of the License at
 *
 *        http://www.apache.org/licenses/LICENSE-2.0
 *
 *    Unless required by applicable law or agreed to in writing, software
 *    distributed under the License is distributed on an "AS IS" BASIS,
 *    WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *    See the License for the specific language governing permissions and
 *    limitations under the License.
 */
package it.smartcommunitylab.aac.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import it.smartcommunitylab.aac.model.Authority;
/**
 * Persistent repository of {@link Authority} entities
 * @author raman
 *
 */
@Repository
public interface AuthorityRepository extends CustomJpaRepository<Authority, String> {

	/**
	 * Find by unique authority redirect path URL
	 * @param redirectUrl
	 * @return
	 */
	@Query("select a from Authority a where a.redirectUrl=?1")
	Authority findByRedirectUrl(String redirectUrl);

}
