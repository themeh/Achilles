package fr.doan.achilles.wrapper;

import static javax.persistence.CascadeType.ALL;
import static javax.persistence.CascadeType.PERSIST;
import static org.fest.assertions.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.lang.reflect.Method;
import java.util.List;

import mapping.entity.UserBean;
import me.prettyprint.cassandra.service.ColumnSliceIterator;
import me.prettyprint.hector.api.beans.Composite;
import me.prettyprint.hector.api.beans.HColumn;

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;
import org.springframework.test.util.ReflectionTestUtils;

import fr.doan.achilles.composite.factory.CompositeKeyFactory;
import fr.doan.achilles.dao.GenericCompositeDao;
import fr.doan.achilles.entity.metadata.EntityMeta;
import fr.doan.achilles.entity.metadata.JoinProperties;
import fr.doan.achilles.entity.metadata.PropertyMeta;
import fr.doan.achilles.entity.metadata.PropertyType;
import fr.doan.achilles.entity.operations.EntityLoader;
import fr.doan.achilles.entity.operations.EntityPersister;
import fr.doan.achilles.entity.type.KeyValueIterator;
import fr.doan.achilles.helper.CompositeHelper;
import fr.doan.achilles.holder.KeyValue;
import fr.doan.achilles.holder.factory.KeyValueFactory;
import fr.doan.achilles.iterator.factory.IteratorFactory;

/**
 * JoinExternalWideMapWrapperTest
 * 
 * @author DuyHai DOAN
 * 
 */
@RunWith(MockitoJUnitRunner.class)
public class JoinExternalWideMapWrapperTest
{
	@Rule
	public ExpectedException expectedEx = ExpectedException.none();

	@InjectMocks
	private JoinExternalWideMapWrapper<Long, Long, Integer, UserBean> wrapper;

	@Mock
	private GenericCompositeDao<Long, Long> dao;

	@Mock
	private PropertyMeta<Integer, UserBean> externalJoinWideMapMeta;

	@Mock
	private CompositeKeyFactory keyFactory;

	@Mock
	private EntityPersister persister;

	@Mock
	private EntityLoader loader;

	@Mock
	private CompositeHelper helper;

	@Mock
	private KeyValueFactory keyValueFactory;

	@Mock
	private IteratorFactory iteratorFactory;

	private Long id = 7425L;

	@Before
	public void setUp()
	{
		ReflectionTestUtils.setField(wrapper, "id", id);
		ReflectionTestUtils.setField(wrapper, "persister", persister);
		ReflectionTestUtils.setField(wrapper, "loader", loader);
		ReflectionTestUtils.setField(wrapper, "helper", helper);
		ReflectionTestUtils.setField(wrapper, "compositeKeyFactory", keyFactory);
		ReflectionTestUtils.setField(wrapper, "keyValueFactory", keyValueFactory);
		ReflectionTestUtils.setField(wrapper, "iteratorFactory", iteratorFactory);
	}

	@Test
	public void should_get_value() throws Exception
	{
		Long joinId = 1235L;
		int key = 4567;
		UserBean userBean = new UserBean();
		Composite comp = new Composite();

		EntityMeta<Long> joinEntityMeta = new EntityMeta<Long>();
		JoinProperties joinProperties = new JoinProperties();
		joinProperties.setEntityMeta(joinEntityMeta);

		when(externalJoinWideMapMeta.getValueClass()).thenReturn(UserBean.class);
		when(externalJoinWideMapMeta.getJoinProperties()).thenReturn(
				(JoinProperties) joinProperties);

		when(keyFactory.createBaseComposite(externalJoinWideMapMeta, key)).thenReturn(comp);
		when(dao.getValue(id, comp)).thenReturn(joinId);
		when(loader.loadJoinEntity(UserBean.class, joinId, joinEntityMeta)).thenReturn(userBean);

		UserBean expected = wrapper.get(key);

		assertThat(expected).isSameAs(userBean);
	}

	@Test
	public void should_insert_value_and_entity_when_insertable() throws Exception
	{

		JoinProperties joinProperties = prepareJoinProperties();
		joinProperties.addCascadeType(PERSIST);

		int key = 4567;
		UserBean userBean = new UserBean();
		long userId = 475L;
		userBean.setUserId(userId);
		Composite comp = new Composite();

		when(externalJoinWideMapMeta.getJoinProperties()).thenReturn(
				(JoinProperties) joinProperties);
		when(keyFactory.createBaseComposite(externalJoinWideMapMeta, key)).thenReturn(comp);
		when(persister.cascadePersistOrEnsureExists(userBean, joinProperties)).thenReturn(userId);

		wrapper.insert(key, userBean);

		verify(dao).setValue(id, comp, userId);
	}

	@Test(expected = IllegalArgumentException.class)
	public void should_exception_when_trying_to_persist_null_entity() throws Exception
	{
		int key = 4567;
		wrapper.insert(key, null);
	}

	@Test
	public void should_insert_value_and_entity_with_ttl() throws Exception
	{
		JoinProperties joinProperties = prepareJoinProperties();
		joinProperties.addCascadeType(ALL);

		int key = 4567;
		UserBean userBean = new UserBean();
		long userId = 475L;
		userBean.setUserId(userId);
		Composite comp = new Composite();

		when(externalJoinWideMapMeta.getJoinProperties()).thenReturn(
				(JoinProperties) joinProperties);
		when(keyFactory.createBaseComposite(externalJoinWideMapMeta, key)).thenReturn(comp);
		when(persister.cascadePersistOrEnsureExists(userBean, joinProperties)).thenReturn(userId);

		wrapper.insert(key, userBean, 150);

		verify(dao).setValue(id, comp, userId, 150);
	}

	@SuppressWarnings(
	{
			"unchecked",
			"rawtypes"
	})
	@Test
	public void should_find_range() throws Exception
	{
		int start = 7, end = 5, count = 10;
		boolean reverse = true, inclusiveStart = false, inclusiveEnd = true;
		Composite startComp = new Composite(), endComp = new Composite();

		when(
				keyFactory.createForQuery(externalJoinWideMapMeta, start, inclusiveStart, end,
						inclusiveEnd, reverse)).thenReturn(new Composite[]
		{
				startComp,
				endComp
		});
		List<HColumn<Composite, ?>> hColumns = mock(List.class);
		when(dao.findRawColumnsRange(id, startComp, endComp, reverse, count)).thenReturn(
				(List) hColumns);
		List<KeyValue<Integer, UserBean>> values = mock(List.class);
		when(
				keyValueFactory.createListForWideRowOrExternalWideMapMeta(externalJoinWideMapMeta,
						hColumns)).thenReturn(values);

		List<KeyValue<Integer, UserBean>> expected = wrapper.findRange(start, inclusiveStart, end,
				inclusiveEnd, reverse, count);

		verify(helper).checkBounds(externalJoinWideMapMeta, start, end, reverse);
		assertThat(expected).isSameAs(values);
	}

	@SuppressWarnings("unchecked")
	@Test
	public void should_get_iterator() throws Exception
	{
		int start = 7, end = 5, count = 10;
		boolean reverse = true, inclusiveStart = false, inclusiveEnd = true;
		Composite startComp = new Composite(), endComp = new Composite();

		when(
				keyFactory.createForQuery(externalJoinWideMapMeta, start, inclusiveStart, end,
						inclusiveEnd, reverse)).thenReturn(new Composite[]
		{
				startComp,
				endComp
		});

		ColumnSliceIterator<Long, Composite, Long> iterator = mock(ColumnSliceIterator.class);
		when(dao.getColumnsIterator(id, startComp, endComp, reverse, count)).thenReturn(iterator);

		KeyValueIterator<Integer, UserBean> keyValueIterator = mock(KeyValueIterator.class);
		when(iteratorFactory.createKeyValueIteratorForWideRow(iterator, externalJoinWideMapMeta))
				.thenReturn(keyValueIterator);

		KeyValueIterator<Integer, UserBean> expected = wrapper.iterator(start, inclusiveStart, end,
				inclusiveEnd, reverse, count);

		assertThat(expected).isSameAs(keyValueIterator);
	}

	@Test
	public void should_remove() throws Exception
	{
		int key = 4567;
		Composite comp = new Composite();

		when(keyFactory.createBaseComposite(externalJoinWideMapMeta, key)).thenReturn(comp);

		wrapper.remove(key);

		verify(dao).removeColumn(id, comp);
	}

	@Test
	public void should_remove_range() throws Exception
	{

		int start = 7, end = 5;
		boolean inclusiveStart = false, inclusiveEnd = true;
		Composite startComp = new Composite(), endComp = new Composite();

		when(
				keyFactory.createForQuery(externalJoinWideMapMeta, start, inclusiveStart, end,
						inclusiveEnd, false)).thenReturn(new Composite[]
		{
				startComp,
				endComp
		});

		wrapper.removeRange(start, inclusiveStart, end, inclusiveEnd);

		verify(helper).checkBounds(externalJoinWideMapMeta, start, end, false);
		verify(dao).removeColumnRange(id, startComp, endComp);

	}

	private JoinProperties prepareJoinProperties() throws Exception
	{
		EntityMeta<Long> joinEntityMeta = new EntityMeta<Long>();
		joinEntityMeta.setClassName("canonicalClassName");

		Method idGetter = UserBean.class.getDeclaredMethod("getUserId");
		PropertyMeta<Void, Long> idMeta = new PropertyMeta<Void, Long>();
		idMeta.setType(PropertyType.SIMPLE);
		idMeta.setGetter(idGetter);
		joinEntityMeta.setIdMeta(idMeta);
		JoinProperties joinProperties = new JoinProperties();
		joinProperties.setEntityMeta(joinEntityMeta);

		return joinProperties;
	}
}
