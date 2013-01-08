package fr.doan.achilles.entity.metadata.builder;

import static fr.doan.achilles.entity.metadata.PropertyType.SIMPLE;
import static fr.doan.achilles.entity.metadata.builder.EntityMetaBuilder.entityMetaBuilder;
import static org.fest.assertions.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Map;

import me.prettyprint.cassandra.model.ExecutingKeyspace;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import parser.entity.Bean;
import fr.doan.achilles.dao.GenericCompositeDao;
import fr.doan.achilles.dao.GenericDynamicCompositeDao;
import fr.doan.achilles.entity.metadata.EntityMeta;
import fr.doan.achilles.entity.metadata.PropertyMeta;
import fr.doan.achilles.entity.metadata.PropertyType;
import fr.doan.achilles.serializer.SerializerUtils;

/**
 * EntityMetaBuilderTest
 * 
 * @author DuyHai DOAN
 * 
 */
@RunWith(MockitoJUnitRunner.class)
public class EntityMetaBuilderTest
{

	@Mock
	private ExecutingKeyspace keyspace;

	@Mock
	private GenericDynamicCompositeDao<?> dao;

	@Mock
	private PropertyMeta<Void, Long> idMeta;

	@SuppressWarnings(
	{
			"unchecked",
			"rawtypes"
	})
	@Test
	public void should_build_meta() throws Exception
	{

		Map<String, PropertyMeta<?, ?>> propertyMetas = new HashMap<String, PropertyMeta<?, ?>>();
		PropertyMeta<Void, String> simpleMeta = new PropertyMeta<Void, String>();
		simpleMeta.setType(SIMPLE);

		Method getter = Bean.class.getDeclaredMethod("getName", (Class<?>[]) null);
		simpleMeta.setGetter(getter);

		Method setter = Bean.class.getDeclaredMethod("setName", String.class);
		simpleMeta.setSetter(setter);

		propertyMetas.put("name", simpleMeta);

		when(idMeta.getValueClass()).thenReturn(Long.class);

		EntityMeta<Long> meta = entityMetaBuilder(idMeta).className("Bean").serialVersionUID(1L)
				.propertyMetas(propertyMetas).keyspace(keyspace).build();

		assertThat(meta.getClassName()).isEqualTo("Bean");
		assertThat(meta.getColumnFamilyName()).isEqualTo("Bean");
		assertThat(meta.getIdMeta()).isSameAs(idMeta);
		assertThat(meta.getIdSerializer().getComparatorType()).isEqualTo(
				SerializerUtils.LONG_SRZ.getComparatorType());
		assertThat(meta.getPropertyMetas()).containsKey("name");
		assertThat(meta.getPropertyMetas()).containsValue(simpleMeta);

		assertThat(meta.getGetterMetas()).hasSize(1);
		assertThat(meta.getGetterMetas().containsKey(getter));
		assertThat(meta.getGetterMetas().get(getter)).isSameAs((PropertyMeta) simpleMeta);

		assertThat(meta.getSetterMetas()).hasSize(1);
		assertThat(meta.getSetterMetas().containsKey(setter));
		assertThat(meta.getSetterMetas().get(setter)).isSameAs((PropertyMeta) simpleMeta);

		assertThat(meta.getEntityDao()).isNotNull();

	}

	@SuppressWarnings("unchecked")
	@Test
	public void should_build_meta_with_column_family_name() throws Exception
	{

		Map<String, PropertyMeta<?, ?>> propertyMetas = new HashMap<String, PropertyMeta<?, ?>>();
		PropertyMeta<Void, String> simpleMeta = new PropertyMeta<Void, String>();
		simpleMeta.setType(SIMPLE);
		propertyMetas.put("name", simpleMeta);

		when(idMeta.getValueClass()).thenReturn(Long.class);

		EntityMeta<Long> meta = entityMetaBuilder(idMeta).className("Bean").serialVersionUID(1L)
				.propertyMetas(propertyMetas).columnFamilyName("toto").keyspace(keyspace).build();

		assertThat(meta.getClassName()).isEqualTo("Bean");
		assertThat(meta.getColumnFamilyName()).isEqualTo("toto");
		assertThat(meta.getWideRowDao()).isNull();
		assertThat(meta.getEntityDao()).isExactlyInstanceOf(GenericDynamicCompositeDao.class);
	}

	@SuppressWarnings("unchecked")
	@Test
	public void should_build_meta_for_wide_row() throws Exception
	{

		Map<String, PropertyMeta<?, ?>> propertyMetas = new HashMap<String, PropertyMeta<?, ?>>();
		PropertyMeta<Integer, String> wideMapMeta = new PropertyMeta<Integer, String>();
		wideMapMeta.setType(PropertyType.WIDE_MAP);
		propertyMetas.put("name", wideMapMeta);

		when(idMeta.getValueClass()).thenReturn(Long.class);

		EntityMeta<Long> meta = entityMetaBuilder(idMeta).className("Bean").serialVersionUID(1L)
				.propertyMetas(propertyMetas).columnFamilyName("toto").keyspace(keyspace)
				.wideRow(true).build();

		assertThat(meta.isWideRow()).isTrue();
		assertThat(meta.getEntityDao()).isNull();
		assertThat(meta.getWideRowDao()).isExactlyInstanceOf(GenericCompositeDao.class);
	}
}
