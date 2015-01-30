package org.kaaproject.kaa.server.common.nosql.cassandra.dao;

import com.datastax.driver.core.querybuilder.Update;
import org.kaaproject.kaa.common.dto.EndpointUserDto;
import org.kaaproject.kaa.server.common.dao.EndpointUserVerifier;
import org.kaaproject.kaa.server.common.dao.impl.EndpointUserDao;
import org.kaaproject.kaa.server.common.nosql.cassandra.dao.model.CassandraEndpointUser;
import org.kaaproject.kaa.server.common.nosql.cassandra.dao.model.CassandraModelConstants;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.UUID;

import static com.datastax.driver.core.querybuilder.QueryBuilder.delete;
import static com.datastax.driver.core.querybuilder.QueryBuilder.eq;
import static com.datastax.driver.core.querybuilder.QueryBuilder.select;
import static com.datastax.driver.core.querybuilder.QueryBuilder.set;
import static com.datastax.driver.core.querybuilder.QueryBuilder.update;
import static com.datastax.driver.core.querybuilder.Select.Where;
import static org.apache.commons.lang.StringUtils.isNotBlank;

public class EndpointUserCassandraDao extends AbstractCassandraDao<CassandraEndpointUser, String> implements EndpointUserDao<CassandraEndpointUser>, EndpointUserVerifier {

    private static final Logger LOG = LoggerFactory.getLogger(EndpointUserCassandraDao.class);

    @Override
    protected Class<CassandraEndpointUser> getColumnFamilyClass() {
        return CassandraEndpointUser.class;
    }

    @Override
    protected String getColumnFamilyName() {
        return CassandraModelConstants.EP_USER_COLUMN_FAMILY_NAME;
    }

    @Override
    public CassandraEndpointUser save(CassandraEndpointUser dto) {
        dto.setId(dto.getExternalId() + CassandraModelConstants.KEY_DELIMITER + dto.getTenantId());
        LOG.trace("Save endpoint user {}", dto);
        return super.save(dto);
    }

    @Override
    public CassandraEndpointUser save(EndpointUserDto dto) {
        return save(new CassandraEndpointUser(dto));
    }

    @Override
    public CassandraEndpointUser findByExternalIdAndTenantId(String externalId, String tenantId) {
        LOG.debug("Try to find endpoint user by external id {} and tenant id {}", externalId, tenantId);
        Where where = select().from(getColumnFamilyName()).where(eq(CassandraModelConstants.EP_USER_EXTERNAL_ID_PROPERTY, externalId)).and(eq(CassandraModelConstants.EP_USER_TENANT_ID_PROPERTY, tenantId));
        LOG.trace("Try to find endpoint user by cql select", where);
        CassandraEndpointUser endpointUser = findOneByStatement(where);
        LOG.trace("Found {} endpoint user", endpointUser);
        return endpointUser;
    }

    @Override
    public void removeByExternalIdAndTenantId(String externalId, String tenantId) {
        LOG.debug("Try to remove endpoint user by external id {} and tenant id {}", externalId, tenantId);
        execute(delete().from(getColumnFamilyName()).where(eq(CassandraModelConstants.EP_USER_EXTERNAL_ID_PROPERTY, externalId)).and(eq(CassandraModelConstants.EP_USER_TENANT_ID_PROPERTY, tenantId)));
    }

    @Override
    public String generateAccessToken(String externalId, String tenantId) {
        LOG.debug("Generating access token for endpoint user with external id {} and tenant id {}", externalId, tenantId);
        String accessToken = UUID.randomUUID().toString();
        Update.Where query = update(getColumnFamilyName()).with(set(CassandraModelConstants.EP_USER_ACCESS_TOKEN_PROPERTY, accessToken))
                .where(eq(CassandraModelConstants.EP_USER_EXTERNAL_ID_PROPERTY, externalId))
                .and(eq(CassandraModelConstants.EP_USER_TENANT_ID_PROPERTY, tenantId));
        execute(query);
        LOG.trace("Generated access token {} for endpoint user by query {}", accessToken, query);
        return accessToken;
    }

    @Override
    public boolean checkAccessToken(String externalId, String tenantId, String accessToken) {
        LOG.debug("Check access token [{}] for endpoint user with external id {} and tenant id {}", accessToken, externalId, tenantId);
        boolean result = false;
        CassandraEndpointUser endpointUser = findByExternalIdAndTenantId(externalId, tenantId);
        if (endpointUser != null && accessToken != null) {
            result = accessToken.equals(endpointUser.getAccessToken());
        }
        return result;
    }

    @Override
    public void removeById(String id) {
        LOG.debug("Try to remove endpoint user by id {}", id);
        if (isNotBlank(id) && id.contains(CassandraModelConstants.KEY_DELIMITER)) {
            String[] compositeId = id.split(CassandraModelConstants.KEY_DELIMITER);
            if (compositeId.length == 2) {
                removeByExternalIdAndTenantId(compositeId[0], compositeId[1]);
            }
        }
    }

    @Override
    public CassandraEndpointUser findById(String id) {
        LOG.debug("Try to find endpoint user by id {}", id);
        CassandraEndpointUser endpointUser = null;
        if (isNotBlank(id) && id.contains(CassandraModelConstants.KEY_DELIMITER)) {
            String[] compositeId = id.split(CassandraModelConstants.KEY_DELIMITER);
            if (compositeId.length == 2) {
                endpointUser = findByExternalIdAndTenantId(compositeId[0], compositeId[1]);
                LOG.trace("Found endpoint user {} by id {}", endpointUser, id);
            }
        }
        return endpointUser;
    }
}
