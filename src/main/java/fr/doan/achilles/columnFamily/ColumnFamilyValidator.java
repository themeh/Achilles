package fr.doan.achilles.columnFamily;

import me.prettyprint.hector.api.ddl.ColumnFamilyDefinition;

import org.apache.commons.lang.StringUtils;

import fr.doan.achilles.entity.PropertyHelper;
import fr.doan.achilles.entity.metadata.EntityMeta;
import fr.doan.achilles.entity.metadata.PropertyMeta;
import fr.doan.achilles.exception.InvalidColumnFamilyException;

/**
 * ColumnFamilyValidator
 * 
 * @author DuyHai DOAN
 * 
 */
public class ColumnFamilyValidator
{
	private PropertyHelper helper = new PropertyHelper();

	private String COMPARATOR_TYPE_AND_ALIAS = "DynamicCompositeType(f=>org.apache.cassandra.db.marshal.FloatType,d=>org.apache.cassandra.db.marshal.DateType,e=>org.apache.cassandra.db.marshal.DecimalType,b=>org.apache.cassandra.db.marshal.BytesType,c=>org.apache.cassandra.db.marshal.BooleanType,a=>org.apache.cassandra.db.marshal.AsciiType,l=>org.apache.cassandra.db.marshal.LongType,j=>org.apache.cassandra.db.marshal.Int32Type,i=>org.apache.cassandra.db.marshal.IntegerType,u=>org.apache.cassandra.db.marshal.UUIDType,t=>org.apache.cassandra.db.marshal.TimeUUIDType,s=>org.apache.cassandra.db.marshal.UTF8Type,z=>org.apache.cassandra.db.marshal.DoubleType,x=>org.apache.cassandra.db.marshal.LexicalUUIDType)";

	public void validateCFWithEntityMeta(ColumnFamilyDefinition cfDef, EntityMeta<?> entityMeta)
	{

		if (!StringUtils.equals(cfDef.getKeyValidationClass(), entityMeta.getIdSerializer()
				.getComparatorType().getClassName()))
		{
			throw new InvalidColumnFamilyException("The column family '"
					+ entityMeta.getColumnFamilyName() + "' key class '"
					+ cfDef.getKeyValidationClass()
					+ "' does not correspond to the entity id class '"
					+ entityMeta.getIdSerializer().getComparatorType().getClassName() + "'");
		}

		if (entityMeta.isWideRow())
		{
			this.validateCFWithPropertyMeta(cfDef, entityMeta.getPropertyMetas().values()
					.iterator().next(), entityMeta.getColumnFamilyName());
		}
		else
		{

			if (cfDef.getComparatorType() == null
					|| !StringUtils.equals(cfDef.getComparatorType().getTypeName(),
							COMPARATOR_TYPE_AND_ALIAS))
			{
				throw new InvalidColumnFamilyException("The column family '"
						+ entityMeta.getColumnFamilyName() + "' comparator type should be '"
						+ COMPARATOR_TYPE_AND_ALIAS + "'");
			}
		}
	}

	public void validateCFWithPropertyMeta(ColumnFamilyDefinition cfDef,
			PropertyMeta<?, ?> propertyMeta, String externalColumnFamilyName)
	{
		String comparatorTypeAlias = helper.determineCompatatorTypeAliasForCompositeCF(
				propertyMeta, false);
		if (cfDef.getComparatorType() == null
				|| !StringUtils
						.equals(cfDef.getComparatorType().getTypeName(), comparatorTypeAlias))
		{
			throw new InvalidColumnFamilyException("The column family '" + externalColumnFamilyName
					+ "' comparator type should be '" + comparatorTypeAlias + "'");
		}
	}
}
