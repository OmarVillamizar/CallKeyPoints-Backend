## Table `calls`

### Columns

| Name | Type | Constraints |
|------|------|-------------|
| `id` | `int8` | Primary Identity |
| `user_id` | `uuid` |  |
| `title` | `text` |  |
| `transcript` | `text` |  |
| `knowledge_base` | `text` |  Nullable |
| `report_extracted_data` | `jsonb` |  Nullable |
| `report_solution` | `text` |  Nullable |
| `report_summary` | `text` |  Nullable |
| `report_generated_at` | `timestamptz` |  Nullable |
| `created_at` | `timestamptz` |  |
| `updated_at` | `timestamptz` |  |