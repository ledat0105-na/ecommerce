// API Vietnam Provinces - Sử dụng API từ provinces.open-api.vn
const API_BASE_URL = 'https://provinces.open-api.vn/api/';

// Cache để lưu dữ liệu đã load
const addressCache = {
    provinces: null,
    districts: {},
    wards: {}
};

/**
 * Load danh sách tỉnh/thành phố
 */
async function loadProvinces(selectElementId) {
    const selectElement = document.getElementById(selectElementId);
    if (!selectElement) return;

    try {
        // Clear existing options except first option
        while (selectElement.options.length > 1) {
            selectElement.remove(1);
        }

        // Check cache
        if (addressCache.provinces) {
            populateSelect(selectElement, addressCache.provinces, 'name', 'code');
            return;
        }

        // Show loading
        const loadingOption = document.createElement('option');
        loadingOption.value = '';
        loadingOption.textContent = 'Đang tải...';
        loadingOption.disabled = true;
        selectElement.appendChild(loadingOption);

        // Fetch from API
        const response = await fetch(API_BASE_URL + 'p/');
        if (!response.ok) {
            throw new Error('Không thể tải danh sách tỉnh/thành phố');
        }

        const provinces = await response.json();
        addressCache.provinces = provinces;

        // Remove loading option
        selectElement.remove(selectElement.options.length - 1);

        // Populate select
        populateSelect(selectElement, provinces, 'name', 'code');
    } catch (error) {
        console.error('Error loading provinces:', error);
        // Remove loading option if exists
        while (selectElement.options.length > 1) {
            selectElement.remove(1);
        }
        const errorOption = document.createElement('option');
        errorOption.value = '';
        errorOption.textContent = 'Không thể tải dữ liệu';
        errorOption.disabled = true;
        selectElement.appendChild(errorOption);
        
        // Initialize Select2 even on error
        if (typeof jQuery !== 'undefined') {
            setTimeout(function() {
                initSelect2(selectElement);
            }, 50);
        }
    }
}

/**
 * Load danh sách quận/huyện theo tỉnh/thành phố
 */
async function loadDistricts(provinceCode, selectElementId) {
    const selectElement = document.getElementById(selectElementId);
    if (!selectElement) return;

    // Clear existing options except first option
    while (selectElement.options.length > 1) {
        selectElement.remove(1);
    }

    // Disable select
    selectElement.disabled = true;
    
    // Update Select2 disabled state
    if (typeof jQuery !== 'undefined' && jQuery(selectElement).hasClass('select2-hidden-accessible')) {
        jQuery(selectElement).prop('disabled', true);
        jQuery(selectElement).trigger('change.select2');
    }

    if (!provinceCode) {
        selectElement.disabled = true;
        // Clear ward select if exists
        const wardSelect = document.getElementById(selectElementId.replace('district', 'ward'));
        if (wardSelect) {
            while (wardSelect.options.length > 1) {
                wardSelect.remove(1);
            }
            wardSelect.disabled = true;
            
            // Update Select2 for ward select
            if (typeof jQuery !== 'undefined' && jQuery(wardSelect).hasClass('select2-hidden-accessible')) {
                jQuery(wardSelect).prop('disabled', true);
                jQuery(wardSelect).trigger('change.select2');
            }
        }
        return;
    }

    try {
        // Check cache
        if (addressCache.districts[provinceCode]) {
            populateSelect(selectElement, addressCache.districts[provinceCode], 'name', 'code');
            selectElement.disabled = false;
            // Update Select2 disabled state
            if (typeof jQuery !== 'undefined') {
                setTimeout(function() {
                    const $select = jQuery(selectElement);
                    $select.prop('disabled', false);
                    if ($select.hasClass('select2-hidden-accessible')) {
                        $select.trigger('change');
                    } else {
                        initSelect2(selectElement);
                    }
                }, 50);
            }
            return;
        }

        // Show loading
        const loadingOption = document.createElement('option');
        loadingOption.value = '';
        loadingOption.textContent = 'Đang tải...';
        loadingOption.disabled = true;
        selectElement.appendChild(loadingOption);

        // Fetch from API
        const response = await fetch(API_BASE_URL + `p/${provinceCode}?depth=2`);
        if (!response.ok) {
            throw new Error('Không thể tải danh sách quận/huyện');
        }

        const province = await response.json();
        const districts = province.districts || [];
        addressCache.districts[provinceCode] = districts;

        // Remove loading option
        selectElement.remove(selectElement.options.length - 1);

        // Populate select
        populateSelect(selectElement, districts, 'name', 'code');
        selectElement.disabled = false;
        
        // Update Select2 disabled state after populating
        if (typeof jQuery !== 'undefined') {
            setTimeout(function() {
                const $select = jQuery(selectElement);
                $select.prop('disabled', false);
                if ($select.hasClass('select2-hidden-accessible')) {
                    $select.trigger('change');
                } else {
                    initSelect2(selectElement);
                }
            }, 50);
        }

        // Clear ward select
        const wardSelect = document.getElementById(selectElementId.replace('district', 'ward'));
        if (wardSelect) {
            while (wardSelect.options.length > 1) {
                wardSelect.remove(1);
            }
            wardSelect.disabled = true;
            
            // Update Select2 for ward select
            if (typeof jQuery !== 'undefined' && jQuery(wardSelect).hasClass('select2-hidden-accessible')) {
                jQuery(wardSelect).prop('disabled', true);
                jQuery(wardSelect).trigger('change.select2');
            }
        }
    } catch (error) {
        console.error('Error loading districts:', error);
        selectElement.disabled = false;
        const errorOption = document.createElement('option');
        errorOption.value = '';
        errorOption.textContent = 'Không thể tải dữ liệu';
        errorOption.disabled = true;
        selectElement.appendChild(errorOption);
    }
}

/**
 * Load danh sách phường/xã theo quận/huyện
 */
async function loadWards(districtCode, selectElementId) {
    const selectElement = document.getElementById(selectElementId);
    if (!selectElement) return;

    // Clear existing options except first option
    while (selectElement.options.length > 1) {
        selectElement.remove(1);
    }

    // Disable select
    selectElement.disabled = true;
    
    // Update Select2 disabled state
    if (typeof jQuery !== 'undefined' && jQuery(selectElement).hasClass('select2-hidden-accessible')) {
        jQuery(selectElement).prop('disabled', true);
        jQuery(selectElement).trigger('change.select2');
    }

    if (!districtCode) {
        selectElement.disabled = true;
        return;
    }

    try {
        // Check cache
        const cacheKey = districtCode;
        if (addressCache.wards[cacheKey]) {
            populateSelect(selectElement, addressCache.wards[cacheKey], 'name', 'code');
            selectElement.disabled = false;
            // Update Select2 disabled state
            if (typeof jQuery !== 'undefined') {
                setTimeout(function() {
                    const $select = jQuery(selectElement);
                    $select.prop('disabled', false);
                    if ($select.hasClass('select2-hidden-accessible')) {
                        $select.trigger('change');
                    } else {
                        initSelect2(selectElement);
                    }
                }, 50);
            }
            return;
        }

        // Show loading
        const loadingOption = document.createElement('option');
        loadingOption.value = '';
        loadingOption.textContent = 'Đang tải...';
        loadingOption.disabled = true;
        selectElement.appendChild(loadingOption);

        // Fetch from API
        const response = await fetch(API_BASE_URL + `d/${districtCode}?depth=2`);
        if (!response.ok) {
            throw new Error('Không thể tải danh sách phường/xã');
        }

        const district = await response.json();
        const wards = district.wards || [];
        addressCache.wards[cacheKey] = wards;

        // Remove loading option
        selectElement.remove(selectElement.options.length - 1);

        // Populate select
        populateSelect(selectElement, wards, 'name', 'code');
        selectElement.disabled = false;
        
        // Update Select2 disabled state after populating
        if (typeof jQuery !== 'undefined') {
            setTimeout(function() {
                const $select = jQuery(selectElement);
                $select.prop('disabled', false);
                if ($select.hasClass('select2-hidden-accessible')) {
                    $select.trigger('change');
                } else {
                    initSelect2(selectElement);
                }
            }, 50);
        }
    } catch (error) {
        console.error('Error loading wards:', error);
        selectElement.disabled = false;
        const errorOption = document.createElement('option');
        errorOption.value = '';
        errorOption.textContent = 'Không thể tải dữ liệu';
        errorOption.disabled = true;
        selectElement.appendChild(errorOption);
    }
}

/**
 * Initialize or refresh Select2 on a select element
 */
function initSelect2(selectElement) {
    if (typeof jQuery === 'undefined') {
        return;
    }
    
    const $select = jQuery(selectElement);
    
    // Get placeholder
    const placeholder = $select.data('placeholder') || $select.find('option[value=""]').text() || '-- Chọn --';
    
    // If Select2 is already initialized, destroy and reinitialize to refresh options
    if ($select.hasClass('select2-hidden-accessible')) {
        // Store current value
        const currentValue = $select.val();
        
        // Destroy Select2
        $select.select2('destroy');
        
        // Reinitialize Select2
        $select.select2({
            theme: 'bootstrap-5',
            language: {
                noResults: function() {
                    return 'Không tìm thấy kết quả';
                },
                searching: function() {
                    return 'Đang tìm kiếm...';
                }
            },
            placeholder: placeholder,
            allowClear: true,
            width: '100%',
            dropdownParent: $select.closest('.mb-3').length ? $select.closest('.mb-3') : jQuery('body')
        });
        
        // Restore value if it was set
        if (currentValue) {
            $select.val(currentValue).trigger('change');
        }
        return;
    }
    
    // Initialize Select2 only if not already initialized
    $select.select2({
        theme: 'bootstrap-5',
        language: {
            noResults: function() {
                return 'Không tìm thấy kết quả';
            },
            searching: function() {
                return 'Đang tìm kiếm...';
            }
        },
        placeholder: placeholder,
        allowClear: true,
        width: '100%',
        dropdownParent: $select.closest('.mb-3').length ? $select.closest('.mb-3') : jQuery('body')
    });
}

/**
 * Populate select element with data
 */
function populateSelect(selectElement, data, textField, valueField) {
    // Store the placeholder option if it exists
    let placeholderOption = null;
    if (selectElement.options.length > 0 && selectElement.options[0].value === '') {
        placeholderOption = selectElement.options[0];
    }
    
    // Clear all existing options
    selectElement.innerHTML = '';
    
    // Restore placeholder option if it existed
    if (placeholderOption) {
        selectElement.appendChild(placeholderOption);
    } else {
        // Create a default placeholder option
        const defaultOption = document.createElement('option');
        defaultOption.value = '';
        defaultOption.textContent = '-- Chọn --';
        selectElement.appendChild(defaultOption);
    }
    
    // Add new options
    if (data && data.length > 0) {
        data.forEach(item => {
            const option = document.createElement('option');
            option.value = item[valueField];
            option.textContent = item[textField];
            selectElement.appendChild(option);
        });
    }
    
    // Initialize or refresh Select2 after populating
    if (typeof jQuery !== 'undefined') {
        // Use setTimeout to ensure DOM is updated
        setTimeout(function() {
            initSelect2(selectElement);
        }, 100);
    }
}

/**
 * Initialize address selects
 * @param {Object} options - Configuration options
 * @param {string} options.provinceSelectId - ID of province select element
 * @param {string} options.districtSelectId - ID of district select element
 * @param {string} options.wardSelectId - ID of ward select element (optional)
 */
function initAddressSelects(options) {
    const { provinceSelectId, districtSelectId, wardSelectId } = options;

    // Function to load provinces (can be called immediately if DOM is ready)
    function startLoading() {
        loadProvinces(provinceSelectId);
    }

    // Load provinces on page load (check if DOM is already ready)
    if (document.readyState === 'loading') {
        document.addEventListener('DOMContentLoaded', startLoading);
    } else {
        // DOM is already ready
        startLoading();
    }

    // Handle province change - use jQuery if available for Select2 compatibility
    if (typeof jQuery !== 'undefined') {
        jQuery(document).on('change', '#' + provinceSelectId, function() {
            const provinceCode = jQuery(this).val();
            loadDistricts(provinceCode, districtSelectId);
        });
    } else {
        const provinceSelect = document.getElementById(provinceSelectId);
        if (provinceSelect) {
            provinceSelect.addEventListener('change', function() {
                const provinceCode = this.value;
                loadDistricts(provinceCode, districtSelectId);
            });
        }
    }

    // Handle district change (if ward select exists) - use jQuery if available for Select2 compatibility
    if (wardSelectId) {
        if (typeof jQuery !== 'undefined') {
            jQuery(document).on('change', '#' + districtSelectId, function() {
                const districtCode = jQuery(this).val();
                loadWards(districtCode, wardSelectId);
            });
        } else {
            const districtSelect = document.getElementById(districtSelectId);
            if (districtSelect) {
                districtSelect.addEventListener('change', function() {
                    const districtCode = this.value;
                    loadWards(districtCode, wardSelectId);
                });
            }
        }
    }
}

