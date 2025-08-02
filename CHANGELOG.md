# CHANGELOG

### v1.5.0+1.21.5 (Architecture Refactoring)
 - **Major architectural refactoring**: Implemented interface-based filter system for improved maintainability
 - **New API structure**: Added ISoundFilter interface, AbstractSoundFilter, AbstractOpenALFilter, and AbstractReverbFilter abstract classes
 - **Improved error handling**: Enhanced OpenAL error detection and recovery mechanisms
 - **Better resource management**: Automatic cleanup of OpenAL resources with proper error handling
 - **Enhanced extensibility**: Simplified process for adding new filter types
 - **Code organization**: Filters now organized in a clear hierarchy (interface → abstract classes → implementations)
 - **Unified filter lifecycle**: Standardized initialization, update, and cleanup processes across all filters
 - **Maintainability improvements**: Eliminated code duplication and improved readability
 - **Performance optimizations**: Reduced overhead through better resource management
 - **Documentation**: Added comprehensive inline documentation for all new API components

### v1.4.0+1.20.6
 - Update to 1.20.6
 - Note: Currently includes custom MODIFIED version of cloth config due to technical reasons.

### v1.4.0+1.20.2
 - Backport to 1.20.2
 - Note: Currently includes custom MODIFIED version of cloth config due to technical reasons.

### v1.4.0+1.20.3
 - Block "Obstruction" and Reverb influence is now defined via names and configurable
 - Slightly reduced performance impact
 - Update to 1.20.3
 - Note: Currently includes custom MODIFIED version of cloth config due to technical reasons.
