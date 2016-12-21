package com.smithies.es09.common;

/**
 * A read alias should point at all active indexes. All reads should be done using this index
 * 
 * A write alias should point at the current active index. All writes should be done using this
 * index
 *
 * The current index should point at the current active index. We should never directly read or
 * write to this index, this is for housekeeping and index creation only.
 *
 */
public final class ElasticSearchTypeAliased {

  private final String typeName;
  private final ElasticSearchIndex readAlias;
  private final ElasticSearchIndex writeAlias;
  private ElasticSearchIndex currentIndex;

  public static ElasticSearchTypeAliased withTypeAndIndex(String type, ElasticSearchIndex index) {
    return new ElasticSearchTypeAliased(type, index, index, index);
  }

  public ElasticSearchTypeAliased(final String type, ElasticSearchIndex readAlias,
      ElasticSearchIndex writeAlias, ElasticSearchIndex current) {
    this.typeName = type;
    this.readAlias = readAlias;
    this.writeAlias = writeAlias;
    this.currentIndex = current;
  }

  /**
   * @return the name of the type
   */
  public String getName() {
    return typeName;
  }

  /**
   * @return All reads should be done using this index
   */
  public ElasticSearchIndex getReadAlias() {
    return readAlias;
  }

  /**
   * @return All writes should be done using this index
   */
  public ElasticSearchIndex getWriteAlias() {
    return writeAlias;
  }

  /**
   * @return We should never directly read or write to this index, this is for housekeeping and
   *         index creation only.
   */
  public ElasticSearchIndex getCurrentIndex() {
    return currentIndex;
  }

  @Override
  public String toString() {
    return "ElasticSearchTypeAliased [name=" + typeName + ", readAlias=" + readAlias
        + ", writeAlias=" + writeAlias + "]";
  }

}
